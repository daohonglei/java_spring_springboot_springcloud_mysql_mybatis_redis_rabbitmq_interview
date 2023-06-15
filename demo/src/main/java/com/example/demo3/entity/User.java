package com.example.demo3.entity;

import lombok.Data;

/**
 * @Title: User
 * @Author: 181514
 * @Date 2023/5/30 17:48
 */

@Data
public class User {

    private String name;
    private int age;
    private DetailInfo detailInfo;

    @Data
    public static class DetailInfo{
        private String firstName;
        private String lastName;

    }
}
