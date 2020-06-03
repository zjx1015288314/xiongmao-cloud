package com.itzjx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author zhaojiexiong
 * @create 2020/6/1
 * @since 1.0.0
 */
@EnableDiscoveryClient
@SpringBootApplication
public class XmCartApplication {
    public static void main(String[] args) {
        SpringApplication.run(XmCartApplication.class,args);
    }
}
