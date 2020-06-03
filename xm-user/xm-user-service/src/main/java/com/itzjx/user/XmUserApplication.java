package com.itzjx.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author zhaojiexiong
 * @create 2020/5/31
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.itzjx.user.mapper")
public class XmUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(XmUserApplication.class, args);
    }
}
