package com.itzjx.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.List;

/**
 * @author zhaojiexiong
 * @create 2020/6/1
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "xm.filter")
public class FilterProperties {
    //白名单列表
    private List<String> allowPaths;
}
