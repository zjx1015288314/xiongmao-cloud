package com.itzjx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author zhaojiexiong
 * @create 2020/6/2
 * @since 1.0.0
 */
@MapperScan("com.itzjx.order.mapper")
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class XmOrderAplication {
    public static void main(String[] args) {
        SpringApplication.run(XmOrderAplication.class,args);
    }
}
