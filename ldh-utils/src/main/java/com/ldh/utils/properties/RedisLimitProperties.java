package com.ldh.utils.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Title: RedisLockProperties
 * @Description:
 * @Author: 181514
 * @Date 2023/5/16 17:05
 */

@ConfigurationProperties(prefix = "spring.redis.limit")
public class RedisLimitProperties {
    private int hashNUmber = 4;


    public int getHashNUmber() {
        return hashNUmber;
    }

    public void setHashNUmber(int hashNUmber) {
        this.hashNUmber = hashNUmber;
    }
}
