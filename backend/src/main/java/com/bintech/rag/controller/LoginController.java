package com.bintech.rag.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.bintech.rag.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final AuthProperties authProperties;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "用户名或密码不能为空"
            ));
        }

        if (!authProperties.getAdminUsername().equals(username) || !authProperties.getAdminPassword().equals(password)) {
            log.warn("登录失败: 用户名或密码错误 - {}", username);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "用户名或密码错误"
            ));
        }

        StpUtil.login("admin");
        log.info("用户登录成功: {}", username);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "登录成功",
                "data", Map.of(
                        "token", StpUtil.getTokenValue(),
                        "username", username
                )
        ));
    }

    @GetMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "已退出登录"
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        if (!StpUtil.isLogin()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "未登录"
            ));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", Map.of(
                        "username", authProperties.getAdminUsername()
                )
        ));
    }
}
