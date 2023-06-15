package com.ldh.utils.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Title: RedisLoginProperties
 * @Description:
 * @Author: 181514
 * @Date 2023/5/16 17:50
 */
@ConfigurationProperties(prefix = "spring.redis.login")
public class RedisLoginProperties {

    private int retryIntervalTime = 5;
    private int maxRetryTimes = 5;

    public int getRetryIntervalTime() {
        return retryIntervalTime;
    }

    public void setRetryIntervalTime(int retryIntervalTime) {
        this.retryIntervalTime = retryIntervalTime;
    }

    public int getMaxRetryTimes() {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }
}
