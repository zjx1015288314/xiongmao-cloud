package com.itzjx.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@SpringCloudApplication
public class XmGateWay {
    public static void main(String[] args) {
        SpringApplication.run(XmGateWay.class,args);
    }
}
