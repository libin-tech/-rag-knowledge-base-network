package com.bin.ragknowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 飞书（Feishu/Lark）开放平台配置属性类
 * 用于读取和管理应用中与飞书服务集成相关的认证配置信息
 * 飞书是一个企业协作平台，本应用通过飞书开放平台实现消息推送和交互功能
 * 配置前缀为 "feishu"，对应 application.yml 中的 feishu 配置节
 */
@Data
@Component
@ConfigurationProperties(prefix = "feishu")
public class FeishuProperties {

    /**
     * 飞书应用 ID（App ID）
     * 在飞书开放平台创建应用后分配的唯一标识
     * 用于标识调用飞书 API 的应用身份
     */
    private String appId;

    /**
     * 飞书应用密钥（App Secret）
     * 与 App ID 配套使用的密钥，用于生成访问令牌
     * 此值属于敏感信息，应妥善保管，不应提交到代码仓库
     */
    private String appSecret;

    /**
     * 验证令牌（Verification Token）
     * 用于验证接收到的事件回调确实来自飞书服务器
     * 在处理飞书推送的事件时，用于校验请求的合法性
     */
    private String verificationToken;

    /**
     * 加密密钥（Encrypt Key）
     * 用于解密飞书推送的加密消息体
     * 如果启用了数据加密功能，飞书会使用此密钥对消息内容进行加密
     * 应用需要使用此密钥进行解密才能读取消息内容
     */
    private String encryptKey;
}
