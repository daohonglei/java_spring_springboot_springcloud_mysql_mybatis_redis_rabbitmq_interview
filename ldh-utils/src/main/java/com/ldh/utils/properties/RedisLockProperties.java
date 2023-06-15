package com.ldh.utils.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Title: RedisLockProperties
 * @Description:
 * @Author: 181514
 * @Date 2023/5/16 17:05
 */

@ConfigurationProperties(prefix = "spring.redis.lock")
public class RedisLockProperties {
    public int baseRetryTime = 30;
    public int baseKeyExpireTime = 30;

    public int getBaseRetryTime() {
        return baseRetryTime;
    }

    public void setBaseRetryTime(int baseRetryTime) {
        this.baseRetryTime = baseRetryTime;
    }

    public int getBaseKeyExpireTime() {
        return baseKeyExpireTime;
    }

    public void setBaseKeyExpireTime(int baseKeyExpireTime) {
        this.baseKeyExpireTime = baseKeyExpireTime;
    }
}
