package com.ldh.utils.comp;

import com.ldh.utils.exception.RedisLimitException;
import com.ldh.utils.properties.RedisLimitProperties;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Title: RedisLimitComponent
 * @Description: redis 限流
 * @Author: 181514
 * @Date 2023/5/23 16:48
 */
@Slf4j
public class RedisLimitComponent {

    private int hashNUmber;
    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Boolean> redisScript;

    private DefaultRedisScript<Boolean> redisConsumptionScript;

    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    public RedisLimitComponent(RedisLimitProperties redisLimitProperties, StringRedisTemplate stringRedisTemplate) {
        this.hashNUmber = redisLimitProperties.getHashNUmber() - 1;
        this.stringRedisTemplate = stringRedisTemplate;
        redisScript = new DefaultRedisScript<>();
        redisScript.setResultType(Boolean.class);
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/redisLimit.lua")));

        redisConsumptionScript = new DefaultRedisScript<>();
        redisConsumptionScript.setResultType(Boolean.class);
        redisConsumptionScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/redisLimitConsumption.lua")));

        for (int i = 0; i <= hashNUmber; i++) {
            int finalI = i;
            executorService.submit(() -> {
                while (true) {
                    try {
                        Map<Object, Object> funcLimit = stringRedisTemplate.opsForHash().entries("funcLimit" + finalI);
                        Set<Map.Entry<Object, Object>> entrySet = funcLimit.entrySet();
                        for (Map.Entry<Object, Object> entry : entrySet) {
                            String hKey = entry.getKey().toString();
                            int index = hKey.lastIndexOf(":");
                            Long addLimit = Long.valueOf(hKey.substring(hKey.lastIndexOf(":", index - 1) + 1, index));
                            Integer permit = Integer.parseInt(entry.getValue().toString());
                            Long increment = Long.valueOf(hKey.substring(index + 1));
                            if (increment > 0 && addLimit >= permit) {
                                stringRedisTemplate.opsForHash().increment("funcLimit" + finalI, entry.getKey(), increment);
                            }
                        }
                        Thread.sleep(1000);
                    } catch (Exception e) {
                        log.error(Thread.currentThread().getName() + " 添加令牌出错 ", e);
                    }
                }
            });
        }
    }

    public void destroy() {
        for (int i = 0; i <= hashNUmber; i++) {
            stringRedisTemplate.delete("funcLimit" + i);
            stringRedisTemplate.delete("funcLimitConsumption" + i);
        }
    }

    public void consumption(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        int h;
        int i = ((h = method.getDeclaringClass().hashCode()) ^ (h >>> 16)) & hashNUmber;

        Limit limit = method.getAnnotation(Limit.class);
        if (limit != null) {
            String key = limit.key();
            if (key == null || "".equals(key)) {
                throw new RedisLimitException("key cannot be null");
            }
            StringBuilder limitKey = new StringBuilder();
            limitKey.append(key).append(":")
                    .append(method.getDeclaringClass().getName()).append(":")
                    .append(method.getName()).append(":")
                    .append(limit.addLimit()).append(":").append(limit.incrementPerSecond());

            log.info(limitKey.toString());

            long defaultPermits = limit.defaultPermits();
            long startTim = System.currentTimeMillis();
            boolean permit = false;
            while ((System.currentTimeMillis() - startTim) <= limit.waitTimes()) {
                try {
                    Boolean result = stringRedisTemplate.execute(redisScript, Arrays.asList("funcLimit" + i, limitKey.toString()),
                            String.valueOf(defaultPermits - 1));
                    log.info("Access try result is {} for key={}", result, limitKey);
                    if (result != null && !result) {
                        log.debug("令牌桶={}，获取令牌失败", limitKey);
                    } else {
                        permit = true;
                        break;
                    }
                } catch (Exception e) {
                    log.error("拿令牌出错", e);
                }
            }
            if (!permit) {
                throw new RedisLimitException(limit.msg());
            }
            stringRedisTemplate.execute(redisConsumptionScript, Arrays.asList("funcLimitConsumption" + i, limitKey.toString()));
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @Documented
    public @interface Limit {

        /**
         * 资源的key,唯一
         * 作用：不同的接口，不同的流量控制
         */
        String key() default "";

        /**
         * 最多的访问限制次数
         */
        long defaultPermits() default 10;

        /**
         * 每次添加的次数
         */
        long incrementPerSecond() default 2;

        /**
         * 等待时间
         */
        long waitTimes() default 0;

        /**
         * 添加令牌的界限
         */
        long addLimit() default 5;

        /**
         * 得不到令牌的提示语
         */
        String msg() default "系统繁忙,请稍后再试.";
    }
}