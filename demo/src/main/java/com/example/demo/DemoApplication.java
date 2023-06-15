package com.example.demo;

import com.ldh.utils.aop.RedisLimitAop;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackageClasses = {RedisLimitAop.class, DemoApplication.class})
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);

        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            System.out.println("addShutdownHook");
        }));
    }
}
