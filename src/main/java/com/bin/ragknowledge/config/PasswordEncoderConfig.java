package com.bin.ragknowledge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置类
 * 配置 BCrypt 密码编码器用于密码加密和验证
 * 
 * BCrypt 是一种基于 Blowfish 加密算法的密码哈希函数，具有以下特点：
 * - 自动加盐：每次哈希都会生成不同的结果，防止彩虹表攻击
 * - 计算成本高：故意设计为计算密集型，防止暴力破解
 * - 不可逆：无法从哈希值反推出原始密码
 * 
 * 该配置类为 Spring Security 提供密码编码器的 Bean 实例
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * 创建 BCryptPasswordEncoder Bean
     * BCrypt 是一种强哈希算法，适合存储密码
     * 
     * 该 Bean 会被 Spring Security 自动使用，用于：
     * - 用户注册时加密存储密码
     * - 用户登录时验证输入的密码与存储的哈希是否匹配
     *
     * @return PasswordEncoder 实例，提供密码加密和验证功能
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用默认的 BCrypt 强度，Spring Security 会自动选择合适的版本
        return new BCryptPasswordEncoder();
    }
}
