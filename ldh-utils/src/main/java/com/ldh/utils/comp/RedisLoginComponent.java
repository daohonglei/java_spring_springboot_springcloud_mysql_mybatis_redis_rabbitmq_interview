package com.ldh.utils.comp;

import com.ldh.utils.properties.RedisLoginProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Title: RedisLockComponent
 * @Author: 181514
 * @Date 2023/5/16 17:09
 */
public class RedisLoginComponent {
    private int retryIntervalTime;

    private int maxRetryTimes;

    private StringRedisTemplate stringRedisTemplate;

    public int[] lock_times = {0, 5, 10, 30, 60};

    private DefaultRedisScript<Boolean> loginFailedScript = new DefaultRedisScript<>();

    public RedisLoginComponent(RedisLoginProperties redisLoginProperties, StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.retryIntervalTime = redisLoginProperties.getRetryIntervalTime();
        this.maxRetryTimes = redisLoginProperties.getMaxRetryTimes();

        loginFailedScript.setResultType(Boolean.class);
        loginFailedScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/loginFailed.lua")));
    }

    public long isLocked(String name){
        String s = stringRedisTemplate.opsForValue().get("lock_" + name);
        if (s!=null || s!=""){
            return stringRedisTemplate.getExpire("lock_" + name);
        }
        return -1;
    }

    public boolean isExceedMaxRetryTimes(String name) {
        int failedTimes = 0;
        for (int i = 0; i < retryIntervalTime; i++) {
            Object object = stringRedisTemplate.opsForHash().get("login" + i, name);
            if (object == null) {
                continue;
            }
            failedTimes = failedTimes + Integer.parseInt((String) object);
            if (failedTimes >= maxRetryTimes) {
                return true;
            }
        }
        return false;
    }

    public void loginSuccess(String name) {
        for (int i = 0; i < retryIntervalTime; i++) {
            stringRedisTemplate.opsForHash().delete("login" + i, name);
        }
        stringRedisTemplate.opsForHash().delete("login_lock", name);
    }

    public void loginFailed(String name) {
        LocalDateTime localDateTime = LocalDateTime.now();
        int minute = localDateTime.getMinute();
        String key = "login" + (minute % retryIntervalTime);
        if (stringRedisTemplate.hasKey(key)) {
            if (stringRedisTemplate.opsForHash().hasKey(key, name)) {
                int times = Integer.parseInt((String) stringRedisTemplate.opsForHash().get(key, name));
                stringRedisTemplate.opsForHash().put(key, name, String.valueOf(++times));
            } else {
                stringRedisTemplate.opsForHash().put(key, name, String.valueOf(1));
            }
        } else {
            // lua 脚本实现
            List<String> keys = Arrays.asList(key, name);
            Boolean result = stringRedisTemplate.execute(loginFailedScript, keys, String.valueOf(retryIntervalTime * 60));
            System.out.println(loginFailedScript.getScriptAsString());
            System.out.println(loginFailedScript.getResultType());
            System.out.println(loginFailedScript.getSha1());
            if (!result) {
                stringRedisTemplate.opsForHash().delete(key, name);
            }
        }
    }

    public void lock(String name) {
        String login_lock = (String) stringRedisTemplate.opsForHash().get("login_lock", name);
        int lockTime = 0;
        if (login_lock == null) {
            lockTime = 1;
        } else {
            lockTime = Integer.valueOf(login_lock) + 1;

        }
        stringRedisTemplate.opsForHash().put("login_lock", name, String.valueOf(lockTime));
        int lock_time = lock_times[lockTime];
        stringRedisTemplate.opsForValue().setIfAbsent("lock_"+name, name, lock_time, TimeUnit.MINUTES);

    }

    public void loginFailedWithTx(String name) {
        LocalDateTime localDateTime = LocalDateTime.now();
        int minute = localDateTime.getMinute();
        String key = "login" + (minute % retryIntervalTime);
        if (stringRedisTemplate.hasKey(key)) {
            if (stringRedisTemplate.opsForHash().hasKey(key, name)) {
                int times = Integer.parseInt((String) stringRedisTemplate.opsForHash().get(key, name));
                stringRedisTemplate.opsForHash().put(key, name, String.valueOf(++times));
            } else {
                stringRedisTemplate.opsForHash().put(key, name, String.valueOf(1));
            }
        } else {
            // 事务实现
            stringRedisTemplate.execute(new SessionCallback<Boolean>() {
                @Override
                public Boolean execute(RedisOperations operations) throws DataAccessException {
                    operations.multi();
                    stringRedisTemplate.opsForHash().put(key, name, String.valueOf(1));
                    stringRedisTemplate.expire(key, retryIntervalTime, TimeUnit.MINUTES);
                    List<Boolean> exec = operations.exec();
                    Boolean transactionResult = exec.stream().reduce(true, (preResult, currentVal) -> preResult && currentVal);
                    return transactionResult;
                }
            });
        }
    }

}
