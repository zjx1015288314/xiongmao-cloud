package com.itzjx.order.config;

import com.itzjx.common.utils.IdWorker;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(IdworkerProperties.class)
public class IdWorkerConfig {

    @Bean
    public IdWorker idWorker(IdworkerProperties prop) {
        return new IdWorker(prop.getWorkerId(), prop.getDatacenterId());
    }
}
