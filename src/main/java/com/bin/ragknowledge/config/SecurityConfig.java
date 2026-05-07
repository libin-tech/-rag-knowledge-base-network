package com.bin.ragknowledge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security 配置类
 * 配置应用的认证和授权规则，保护应用的安全性
 * 
 * 主要功能：
 * - 启用 Spring Security 的 Web 安全功能（@EnableWebSecurity）
 * - 启用方法级别的安全注解（@EnableMethodSecurity），支持 @PreAuthorize、@PostAuthorize 等
 * - 配置 HTTP 请求的访问控制规则
 * - 配置表单登录和登出功能
 * - 配置 AuthenticationManager 用于用户认证
 * 
 * 该类定义了哪些接口需要认证，哪些可以公开访问，以及登录和登出的具体行为
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * 配置 Security 过滤链
     * 定义 HTTP 请求的访问规则和登录配置
     * 
     * 该方法配置了以下安全策略：
     * 1. CSRF 保护：禁用 CSRF 令牌检查（适用于无状态 API 或非浏览器客户端）
     * 2. 授权规则：定义哪些 URL 可以公开访问，哪些需要认证
     * 3. 表单登录：配置登录页面、登录处理 URL、登录成功/失败跳转
     * 4. 登出配置：配置登出 URL、登出后的行为和清理操作
     *
     * @param http HttpSecurity 对象，用于配置 HTTP 级别的安全策略
     * @return SecurityFilterChain 实例，包含所有安全过滤器的配置
     * @throws Exception 配置过程中可能出现的异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（跨站请求伪造）保护
                // 适用于 API 服务或不使用 Cookie 认证的场景
                // 如果使用 Session 认证，建议启用 CSRF 保护
                .csrf(AbstractHttpConfigurer::disable)

                // 配置 HTTP 请求的授权规则
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**", "/static/**").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/admin/**").authenticated()
                        .anyRequest().authenticated())
                // 配置帧选项，允许同源页面在 iframe 中显示
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))

                // 配置表单登录功能
                .formLogin(form -> form
                        // 设置自定义的登录页面 URL
                        .loginPage("/login")
                        // 设置处理登录表单提交请求的 URL
                        // 与 loginPage 相同表示登录页面和登录处理使用同一个 URL
                        .loginProcessingUrl("/login")
                        // 设置登录成功后的默认跳转页面
                        // true 表示始终跳转到此 URL，忽略登录前的请求页面
                        .defaultSuccessUrl("/admin/upload", true)
                        // 设置登录失败后的跳转页面
                        // 添加 error=true 参数用于显示错误提示
                        .failureUrl("/login?error=true")
                        // 允许所有用户访问登录相关接口
                        .permitAll())

                // 配置用户登出功能
                .logout(logout -> logout
                        // 设置处理登出请求的 URL
                        .logoutUrl("/logout")
                        // 设置登出成功后的跳转页面
                        // 添加 logout=true 参数用于显示登出成功提示
                        .logoutSuccessUrl("/login?logout=true")
                        // 登出时使当前用户的 Session 失效
                        // 确保用户数据不会在服务器端残留
                        .invalidateHttpSession(true)
                        // 登出时删除指定的 Cookie
                        // 删除 JSESSIONID 以清除 Session 标识
                        .deleteCookies("JSESSIONID")
                        // 允许所有用户执行登出操作
                        .permitAll());

        // 构建并返回 SecurityFilterChain 实例
        return http.build();
    }

    /**
     * 配置 AuthenticationManager Bean
     * AuthenticationManager 是 Spring Security 的核心组件，负责处理用户认证逻辑
     * 
     * 该方法从 AuthenticationConfiguration 中获取 AuthenticationManager 实例
     * 并将其注册为 Spring Bean，以便在其他地方（如登录过滤器）中使用
     *
     * @param authenticationConfiguration Spring Security 的认证配置对象
     * @return AuthenticationManager 实例，用于执行用户认证操作
     * @throws Exception 获取 AuthenticationManager 时可能出现的异常
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        // 从配置对象中提取 AuthenticationManager
        // 该 Manager 会使用我们配置的 UserDetailsService 和 PasswordEncoder 进行认证
        return authenticationConfiguration.getAuthenticationManager();
    }
}
