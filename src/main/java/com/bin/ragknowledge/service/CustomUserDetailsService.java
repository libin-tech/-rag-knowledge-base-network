package com.bin.ragknowledge.service;

import com.bin.ragknowledge.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自定义用户详情服务
 * <p>
 * 职责：实现 Spring Security 的 UserDetailsService 接口，负责用户认证过程中的
 * 用户详情加载工作。当前采用基于配置文件的简单认证策略，适用于小型内部系统
 * 或开发测试环境。
 * </p>
 *
 * <p>认证工作流程：</p>
 * <ol>
 *   <li>Spring Security 在用户登录时调用 loadUserByUsername 方法</li>
 *   <li>校验输入的用户名是否与配置的管理员账号匹配</li>
 *   <li>使用 BCrypt 算法对配置的密码进行加密处理</li>
 *   <li>构建并返回 UserDetails 对象，包含用户名、加密密码和权限信息</li>
 *   <li>Spring Security 将用户输入的密码与返回的加密密码进行比对完成认证</li>
 * </ol>
 *
 * <p>注意事项：</p>
 * <ul>
 *   <li>当前实现为简化版本，用户名和密码从配置文件（AuthProperties）中读取</li>
 *   <li>生产环境建议切换为数据库查询方式，支持多用户管理和密码修改功能</li>
 *   <li>密码采用 BCrypt 强哈希算法，每次加密都会生成不同的盐值</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /** 认证配置属性，包含管理员用户名和密码等认证相关配置 */
    private final AuthProperties authProperties;

    /** 密码编码器，使用 BCrypt 算法对密码进行加密和校验 */
    private final PasswordEncoder passwordEncoder;

    /**
     * 根据用户名加载用户详情信息
     * <p>
     * 工作流程：
     * 1. 校验输入的用户名是否与配置的管理员账号完全匹配
     * 2. 若不匹配，记录警告日志并抛出 UsernameNotFoundException
     * 3. 若匹配，使用 BCrypt 对配置的管理员密码进行加密
     * 4. 构建 UserDetails 对象，设置用户名、加密密码和管理员角色权限
     * 5. 记录成功登录日志并返回 UserDetails 对象
     * </p>
     * <p>
     * 重要说明：Spring Security 在认证时会将用户输入的明文密码与此方法返回的
     * UserDetails 中的加密密码进行比对（通过 PasswordEncoder.matches 方法），
     * 因此这里需要返回 BCrypt 加密后的密码而非明文密码。
     * </p>
     *
     * @param username 用户登录时输入的用户名
     * @return UserDetails 包含用户信息的 Spring Security 用户详情对象
     * @throws UsernameNotFoundException 当输入的用户名与配置的管理员账号不匹配时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 校验用户名是否与配置的管理员账号匹配
        // 注意：此处使用精确匹配，区分大小写
        if (!authProperties.getAdminUsername().equals(username)) {
            // 记录登录失败日志，便于安全审计和问题排查
            log.warn("登录失败: 用户不存在 - {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        // 构建用户详情对象
        // 使用 BCrypt 加密密码，每次加密都会生成不同的盐值，但校验时会得到相同结果
        String encodedPassword = passwordEncoder.encode(authProperties.getAdminPassword());

        // 记录用户成功登录日志
        log.info("用户登录成功: {}", username);

        // 使用 Spring Security 的 User.builder 构建用户详情对象
        return User.builder()
                .username(authProperties.getAdminUsername())           // 设置用户名
                .password(encodedPassword)                             // 设置 BCrypt 加密后的密码
                .authorities(Collections.singletonList(() -> "ROLE_ADMIN"))  // 赋予管理员角色权限
                .build();
    }
}
