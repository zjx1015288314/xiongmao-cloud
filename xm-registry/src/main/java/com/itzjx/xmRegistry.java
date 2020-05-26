package com.itzjx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class xmRegistry {
    public static void main(String[] args) {
        SpringApplication.run(xmRegistry.class,args);
    }
}
