package com.itzjx.upload.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zhaojiexiong
 * @create 2020/5/25
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "xm.upload")
public class UploadProperties {
    private String baseUrl;
    private List<String> allowTypes;
}
