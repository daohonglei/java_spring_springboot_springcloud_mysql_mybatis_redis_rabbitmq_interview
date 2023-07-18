package com.example.demo.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.ldh.utils.comp.RedisLimitComponent;
import com.ldh.utils.comp.RedisLockComponent;
import com.ldh.utils.comp.RedisLoginComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @Title: TestController
 * @Author: 181514
 * @Date 2023/5/11 13:53
 */
@Slf4j
@RestController
public class TestController {

    private final RateLimiter limiter = RateLimiter.create(1.0);

    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RedisLockComponent redisLockComponent;

    @Autowired
    private RedisLoginComponent redisLoginComponent;

    Random random = new Random();

    @GetMapping("lock")
    public String test() {
        if (redisLockComponent.lock("lockTest", Thread.currentThread().getName(), 30,30,true)) {
            try {
                System.out.println(Thread.currentThread().getName() + " lock success");
                int i = random.nextInt(50) * 1000;
                System.out.println(Thread.currentThread().getName() + " do work  " + i);
                Thread.sleep(i);
                System.out.println(Thread.currentThread().getName() + " lock release");
            } finally {
                redisLockComponent.unlock("lockTest", Thread.currentThread().getName());
                return "lock success";
            }
        } else {
            System.out.println(Thread.currentThread().getName() + " lock failed");
            return "lock failed";
        }
    }

    @GetMapping("reentrantLock")
    public String reentrantLock() {
        System.out.println(Thread.currentThread().getName() + " star get lock");
        if (redisLockComponent.reentrantLock("reentrantLock", Thread.currentThread().getName())) {
            System.out.println(Thread.currentThread().getName() + " reentrantLock success");
            try {
                if (redisLockComponent.reentrantLock("reentrantLock", Thread.currentThread().getName())) {
                    System.out.println(Thread.currentThread().getName() + " reentrantLock success");
                    try {
                        int i = random.nextInt(50) * 1000;
                        System.out.println(Thread.currentThread().getName() + " do work  " + i);
                        Thread.sleep(i);
                    } finally {
                        System.out.println(Thread.currentThread().getName() + " lock release");
                        redisLockComponent.reentrantUnLock("reentrantLock", Thread.currentThread().getName());
                    }
                } else {
                    System.out.println(Thread.currentThread().getName() + " reentrantLock failed");
                }
            } finally {
                System.out.println(Thread.currentThread().getName() + " lock release");
                redisLockComponent.reentrantUnLock("reentrantLock", Thread.currentThread().getName());
                return "lock success";
            }
        } else {
            System.out.println(Thread.currentThread().getName() + " lock failed");
            return "lock failed";
        }
    }

    @GetMapping("login")
    public String login(String name, String pwd) {
        long locked = redisLoginComponent.isLocked(name);
        if (locked > 0) {
            return "请在" + ((locked - 1) / 60 + 1) + "分后重试";
        }

        if (redisLoginComponent.isExceedMaxRetryTimes(name)) {
            return "超过最大错误次数";
        }

        if ("张三".equals(name) && "123".equals(pwd)) {
            redisLoginComponent.loginSuccess(name);
            return "login success";
        } else {
            redisLoginComponent.loginFailed(name);
            if (redisLoginComponent.isExceedMaxRetryTimes(name)) {
                redisLoginComponent.lock(name);
            }
            return "login failed";
        }
    }

    @GetMapping("limit2")
    @RedisLimitComponent.Limit(key = "redis-limit:limit2", defaultPermits = 10, incrementPerSecond = 0, addLimit = 5, waitTimes = 100, msg = "当前排队人数较多，请稍后再试！")
    public String limit2() {
        return "limit2";
    }

    @GetMapping("limit3")
    @RedisLimitComponent.Limit(key = "redis-limit:limit3", defaultPermits = 100, incrementPerSecond = 40, addLimit = 60, waitTimes = 100, msg = "当前排队人数较多，请稍后再试！")
    public String limit3() {
        return "limit3";
    }

    @GetMapping("/test1")
    public String testLimiter() {
        boolean tryAcquire = limiter.tryAcquire(500, TimeUnit.MILLISECONDS);
        if (!tryAcquire) {
            log.warn("进入服务降级，时间{}", LocalDateTime.now().format(dtf));
            return "当前排队人数较多，请稍后再试！";
        }
        log.info("获取令牌成功，时间{}", LocalDateTime.now().format(dtf));
        return "请求成功";
    }

    @RequestMapping("/test")
    public String testTest() {
        log.info("this is a log from spring boot!");
        return  new Date().toLocaleString();
    }
}