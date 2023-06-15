package com.example.demo3;

import com.ldh.utils.aop.RedisLimitAop;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackageClasses = {RedisLimitAop.class, Demo3Application.class})
public class Demo3Application {

    public static void main(String[] args) {
        SpringApplication.run(Demo3Application.class, args);

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("addShutdownHook");
        }));
    }
}
