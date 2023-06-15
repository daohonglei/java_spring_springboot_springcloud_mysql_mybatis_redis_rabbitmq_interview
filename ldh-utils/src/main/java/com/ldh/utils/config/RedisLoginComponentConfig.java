package com.ldh.utils.config;

import com.ldh.utils.comp.RedisLoginComponent;
import com.ldh.utils.properties.RedisLoginProperties;
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
@EnableConfigurationProperties(RedisLoginProperties.class)
public class RedisLoginComponentConfig {

    @Autowired
    private RedisLoginProperties redisLoginProperties;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Bean
    @ConditionalOnMissingBean(RedisLoginComponent.class)
    @ConditionalOnClass({RedisLoginComponent.class, StringRedisTemplate.class})
    @ConditionalOnProperty(prefix = "spring.redis.login", value = "enabled", matchIfMissing = true)
    public RedisLoginComponent redisLoginComponent() {
        return new RedisLoginComponent(redisLoginProperties, stringRedisTemplate);
    }
}
