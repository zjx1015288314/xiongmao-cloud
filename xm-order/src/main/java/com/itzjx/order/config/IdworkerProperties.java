package com.itzjx.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhaojiexiong
 * @create 2020/6/2
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "xm.worker")
public class IdworkerProperties {
    private long workerId;// 当前机器id

    private long datacenterId;// 序列号
}
