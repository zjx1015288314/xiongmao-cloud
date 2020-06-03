package com.itzjx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author zhaojiexiong
 * @create 2020/5/28
 * @since 1.0.0
 */
@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
public class XmPageApplication {
    public static void main(String[] args) {
        SpringApplication.run(XmPageApplication.class,args);
    }
}
