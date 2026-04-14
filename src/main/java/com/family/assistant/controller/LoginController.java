package com.family.assistant.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 登录页面控制器
 * 负责处理登录页面的路由请求，展示登录表单并根据登录状态显示相应的提示信息。
 * 支持登录失败错误提示和登出成功提示。
 */
@Controller
public class LoginController {

    /**
     * 显示登录页面
     * 处理 GET /login 请求，根据参数显示相应的错误或提示信息
     *
     * @param error 登录失败标识，当值不为 null 时向页面显示"用户名或密码错误"
     * @param logout 登出成功标识，当值不为 null 时向页面显示"已成功登出"
     * @param model Spring MVC 数据模型，用于向视图传递提示信息
     * @return 登录页面的视图名称 "login"
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        // 如果登录失败，添加错误信息到模型，前端会显示红色错误提示
        if (error != null) {
            model.addAttribute("error", "用户名或密码错误");
        }

        // 如果登出成功，添加提示信息到模型，前端会显示绿色成功提示
        if (logout != null) {
            model.addAttribute("message", "已成功登出");
        }

        return "login";
    }
}
