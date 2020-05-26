package com.itzjx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

@EnableDiscoveryClient
@SpringBootApplication
@MapperScan("com.itzjx.item.mapper")
public class XmItemApplication {
    public static void main(String[] args) {
        SpringApplication.run(XmItemApplication.class,args);
    }
}
