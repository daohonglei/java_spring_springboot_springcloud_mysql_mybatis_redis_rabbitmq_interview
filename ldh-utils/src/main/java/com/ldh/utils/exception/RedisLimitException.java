package com.ldh.utils.exception;

/**
 * @author JAVA日知录
 * Redis限流自定义异常
 * @date 2022/5/2 21:43
 */
public class RedisLimitException extends RuntimeException {
    public RedisLimitException(String msg) {
        super(msg);
    }
}
