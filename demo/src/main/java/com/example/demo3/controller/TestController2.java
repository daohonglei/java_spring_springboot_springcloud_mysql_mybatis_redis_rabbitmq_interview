package com.example.demo3.controller;

import com.example.demo3.entity.User;
import com.ldh.utils.aop.RedisLockAop;
import com.ldh.utils.comp.RedisLimitComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Title: TestController
 * @Author: 181514
 * @Date 2023/5/11 13:53
 */
@Slf4j
@RestController
public class TestController2 {

    @GetMapping("limit22")
    @RedisLimitComponent.Limit(key = "redis-limit:limit2", defaultPermits = 10, incrementPerSecond = 0, addLimit = 5, waitTimes = 100, msg = "当前排队人数较多，请稍后再试！")
    @RedisLockAop.Lock(key = "lock22", lockTimes = 30, waitTimes = 10)
    public String limit2() throws InterruptedException {
        Thread.sleep(40000);
        return "limit2";
    }

    @GetMapping("limit23")
    @RedisLimitComponent.Limit(key = "redis-limit:limit3", defaultPermits = 100, incrementPerSecond = 30, addLimit = 50, waitTimes = 100, msg = "当前排队人数较多，请稍后再试！")
    @RedisLockAop.Lock(key = "TestController2_limit3", lockTimes = 30, waitTimes = 10, ownerIndex = -1)
    public String limit3(String name) throws InterruptedException {
        Thread.sleep(40000);
        return "limit3";
    }

    @GetMapping("user")
    @RedisLimitComponent.Limit(key = "redis-limit:user", defaultPermits = 100, incrementPerSecond = 30, addLimit = 50, waitTimes = 100, msg = "当前排队人数较多，请稍后再试！")
    @RedisLockAop.Lock(key = "user", lockTimes = 30, waitTimes = 10, ownerIndex = 0, ownerName = "detailInfo.firstName", enable = true, expiredRenewal = true, msg = "当前排队人数较多，请稍后再试！")
    public String user(@RequestBody User user) throws InterruptedException {
        Thread.sleep(40000);
        return "limit3";
    }
}