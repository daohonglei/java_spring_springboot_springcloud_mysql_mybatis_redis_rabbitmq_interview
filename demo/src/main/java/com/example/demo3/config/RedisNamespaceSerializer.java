package com.example.demo3.config;

import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class RedisNamespaceSerializer extends StringRedisSerializer {
    private String namespace;
    private Charset charset;

    public RedisNamespaceSerializer(String namespace) {
        this(namespace, StandardCharsets.UTF_8);
    }

    public RedisNamespaceSerializer(String namespace, Charset charset) {
        this.namespace = namespace;
        this.charset = charset;
    }

    @Override
    public String deserialize(byte[] bytes) {
        String key = new String(bytes, charset);
        int index = key.indexOf(namespace);
        if (index == 0) {
            key = key.substring(namespace.length() + 1);
        }
        return key;
    }

    @Override
    public byte[] serialize(String string) {
        if (string == null) {
            return null;
        }
        String key = namespace + "." + string;
        return key.getBytes(charset);
    }
}