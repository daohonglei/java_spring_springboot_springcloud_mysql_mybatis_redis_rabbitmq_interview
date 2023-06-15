package com.example.demo3.comp;

import com.ldh.utils.comp.RedisLimitComponent;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;

/*@Slf4j
@Aspect
@Component*/
public class RedisLimitAop {

    @Autowired
    private RedisLimitComponent redisLimitComponent;


    @Pointcut("@annotation(com.ldh.utils.comp.RedisLimitComponent.Limit)")
    private void check() {
    }


    @PreDestroy
    public void destroy() {
        redisLimitComponent.destroy();
    }


    @Before("check()")
    public void before(JoinPoint joinPoint) {
        redisLimitComponent.consumption(joinPoint);
    }
}