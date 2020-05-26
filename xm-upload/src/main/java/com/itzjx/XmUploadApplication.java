package com.itzjx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * @author zhaojiexiong
 * @create 2020/5/24
 * @since 1.0.0
 */
@EnableEurekaClient
@SpringBootApplication
public class XmUploadApplication {
    public static void main(String[] args) {
        SpringApplication.run(XmUploadApplication.class,args);
    }
}
