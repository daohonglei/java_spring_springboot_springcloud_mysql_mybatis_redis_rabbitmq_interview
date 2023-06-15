package com.ldh.utils.aop;

import com.ldh.utils.comp.RedisLockComponent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @Title: RedisLockAop
 * @Author: 181514
 * @Date 2023/5/29 16:54
 */

@Slf4j
@Aspect
@Component
public class RedisLockAop {

    @Autowired
    private RedisLockComponent redisLockComponent;

    @Pointcut("@annotation(com.ldh.utils.aop.RedisLockAop.Lock)")
    private void check() {

    }

    @Around("check()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Lock lock = method.getAnnotation(Lock.class);
        if (lock.enable()) {
            Object[] values = joinPoint.getArgs();
            Object ownerValue = null;
            if (values.length > 0 && lock.ownerIndex() >= 0) {
                ownerValue = values[lock.ownerIndex()];
                if (StringUtils.hasText(lock.ownerName())) {
                    String[] split = lock.ownerName().split("\\.");
                    for (String string : split) {
                        if (ownerValue != null) {
                            ownerValue = this.getFieldValueByFieldName(string, ownerValue);
                        }
                    }
                }
            }

            String key = null;
            if (ownerValue != null) {
                key = lock.key() + "_" + lock.ownerName() + "_" + ownerValue;
            } else {
                key = lock.key() + "_" + lock.ownerName();
                ownerValue = Thread.currentThread().getName();
            }
            if (redisLockComponent.lock(key, ownerValue.toString(), lock.lockTimes(), lock.waitTimes(), lock.expiredRenewal())) {
                Object returnVale;
                try {
                    returnVale = joinPoint.proceed();
                    return returnVale;
                } catch (Exception e) {
                    throw e;
                } finally {
                    redisLockComponent.unlock(key, ownerValue.toString());
                }
            } else {
                throw new RuntimeException(lock.msg());
            }
        } else {
            return joinPoint.proceed();
        }
    }

    private Object getFieldValueByFieldName(String fieldName, Object object) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) {
            return null;
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    @Documented
    public @interface Lock {
        String key() default "";

        /**
         * 等待时间
         */
        int waitTimes() default 30;

        /**
         * 每次添加的次数
         */
        int lockTimes() default 30;

        /**
         * 得不到令牌的提示语
         */
        String msg() default "系统繁忙,请稍后再试.";

        int ownerIndex() default 0;

        String ownerName() default "";

        boolean enable() default true;

        boolean expiredRenewal() default true;
    }

}
