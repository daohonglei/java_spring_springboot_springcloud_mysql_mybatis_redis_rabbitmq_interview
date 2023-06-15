package com.ldh.utils.config;

import com.ldh.utils.comp.RedisLimitComponent;
import com.ldh.utils.comp.RedisLockComponent;
import com.ldh.utils.properties.RedisLimitProperties;
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
@EnableConfigurationProperties(RedisLimitProperties.class)
@ConditionalOnClass(StringRedisTemplate.class)
public class RedisLimitComponentConfig {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisLimitProperties redisLimitProperties;


    @Bean
    @ConditionalOnMissingBean(RedisLimitComponent.class)
    @ConditionalOnClass({RedisLimitComponent.class, StringRedisTemplate.class})
    @ConditionalOnProperty(prefix = "spring.redis.limit", value = "enabled", matchIfMissing = true)
    public RedisLimitComponent redisLimitComponent() {
        return new RedisLimitComponent(redisLimitProperties, stringRedisTemplate);
    }

}
