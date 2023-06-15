package com.example.demo3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @Title: AppConfig
 * @Author: 181514
 * @Date 2023/6/12 11:11
 */

@Configuration
public class AppConfig {

    @Value("${spring.redis.namespace}")
    private String namespace;

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setKeySerializer(new RedisNamespaceSerializer(namespace));
        template.setConnectionFactory(factory);
        return template;
    }


}
