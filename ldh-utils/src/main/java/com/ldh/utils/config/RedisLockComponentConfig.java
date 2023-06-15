package com.ldh.utils.config;

import com.ldh.utils.comp.RedisLockComponent;
import com.ldh.utils.properties.RedisLockProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @Title: RedisLockComponentConfig
 * @Author: 181514
 * @Date 2023/5/16 17:11
 */


@Configuration
@EnableConfigurationProperties(RedisLockProperties.class)
public class RedisLockComponentConfig {
    @Autowired
    private RedisLockProperties redisLockProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Bean
    @ConditionalOnMissingBean(RedisLockComponent.class)
    @ConditionalOnClass({RedisLockComponent.class, StringRedisTemplate.class})
    @ConditionalOnProperty(prefix = "spring.redis.lock", value = "enabled", matchIfMissing = true)
    public RedisLockComponent redisLockComponent() {
        return new RedisLockComponent(redisLockProperties, stringRedisTemplate);
    }

}
