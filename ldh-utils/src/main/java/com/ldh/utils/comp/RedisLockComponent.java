package com.ldh.utils.comp;

import com.ldh.utils.properties.RedisLockProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @Title: RedisLockComponent
 * @Author: 181514
 * @Date 2023/5/16 17:09
 */
@Slf4j
public class RedisLockComponent {
    public int baseRetryTime = 30;
    public int baseKeyExpireTime = 30;

    private StringRedisTemplate stringRedisTemplate;

    private DefaultRedisScript<Boolean> unlockScript = new DefaultRedisScript<>();
    private DefaultRedisScript<Boolean> reentrantLockScript = new DefaultRedisScript<>();
    private DefaultRedisScript<Boolean> reentrantUnLockScript = new DefaultRedisScript<>();
    private DefaultRedisScript<Boolean> expiredRenewalScript = new DefaultRedisScript<>();
    private DefaultRedisScript<Boolean> reentrantExpiredRenewalScript = new DefaultRedisScript<>();

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    public RedisLockComponent(RedisLockProperties redisLockProperties, StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.baseRetryTime = redisLockProperties.getBaseRetryTime();
        this.baseKeyExpireTime = redisLockProperties.getBaseKeyExpireTime();

        unlockScript.setResultType(Boolean.class);
        unlockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/unlock.lua")));
        expiredRenewalScript.setResultType(Boolean.class);
        expiredRenewalScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/expiredRenewal.lua")));

        reentrantLockScript.setResultType(Boolean.class);
        reentrantLockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/reentrantLock.lua")));

        reentrantUnLockScript.setResultType(Boolean.class);
        reentrantUnLockScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/reentrantUnLock.lua")));

        reentrantExpiredRenewalScript.setResultType(Boolean.class);
        reentrantExpiredRenewalScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/reentrantExpiredRenewal.lua")));
    }

    public boolean lock(String key, String value, int expireTimes, int waitTimes, boolean expiredRenewal) {
        long beginTimes = System.currentTimeMillis();
        expireTimes = expireTimes > 0 ? expireTimes : baseKeyExpireTime;
        waitTimes = waitTimes > 0 ? waitTimes : baseRetryTime;

        while (((System.currentTimeMillis() - beginTimes) / 1000) < waitTimes) {
            if (stringRedisTemplate.opsForValue().setIfAbsent(key, value, expireTimes, TimeUnit.SECONDS)) {
                if (expiredRenewal) {
                    Thread thread = new Thread(() -> {
                        while (true) {
                            try {
                                Thread.sleep(5000);
                                List<String> keys = Collections.singletonList(key);
                                Boolean result = stringRedisTemplate.execute(expiredRenewalScript, keys, value, "5", "15");
                                if (!result) {
                                    return;
                                }
                            } catch (Exception e) {
                                log.error("锁续期错误", e);
                            }
                        }
                    });
                    thread.setDaemon(true);
                    executorService.submit(thread);
                }
                return true;
            }
        }
        return false;
    }

    public void unlock(String key, String value) {
        List<String> keys = Collections.singletonList(key);
        stringRedisTemplate.execute(unlockScript, keys, value);
    }

    public boolean reentrantLock(String key, String hashKey) {
        long beginTimes = System.currentTimeMillis();
        while (((System.currentTimeMillis() - beginTimes) / 1000) < baseRetryTime) {
            List<String> keys = Arrays.asList(key, hashKey);
            boolean result = stringRedisTemplate.execute(reentrantLockScript, keys, String.valueOf(baseKeyExpireTime));
            if (result) {
                String string = (String) stringRedisTemplate.opsForHash().get(key, hashKey);
                if ("1".equals(string)) {
                    Thread thread = new Thread(() -> {
                        while (true) {
                            try {
                                Thread.sleep(5000);
                                Boolean result2 = stringRedisTemplate.execute(reentrantExpiredRenewalScript, keys, "5", "15");
                                if (!result2) {
                                    return;
                                }
                            } catch (Exception e) {
                                log.error("锁续期错误", e);
                            }
                        }
                    });
                    thread.setDaemon(true);
                    executorService.submit(thread);
                }
                return true;
            }
        }
        return false;
    }

    public boolean reentrantUnLock(String key, String hashKey) {
        List<String> keys = Arrays.asList(key, hashKey);
        return stringRedisTemplate.execute(reentrantUnLockScript, keys);
    }

}
