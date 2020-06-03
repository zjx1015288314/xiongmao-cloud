package com.itzjx;

import com.itzjx.sms.config.SmsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * @author zhaojiexiong
 * @create 2020/5/31
 * @since 1.0.0
 */
@SpringBootApplication
public class XmSmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(XmSmsApplication.class,args);
    }
}
