package com.bin.ragknowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 认证配置属性类
 * 用于读取 application.yml 中的 auth 配置项
 */
@Data
@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /**
     * 管理员用户名
     * 默认值: admin
     */
    private String adminUsername = "admin";

    /**
     * 管理员密码
     * 默认值: admin123
     */
    private String adminPassword = "admin123";
}
