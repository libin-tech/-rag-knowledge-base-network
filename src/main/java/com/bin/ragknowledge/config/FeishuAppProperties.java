package com.bin.ragknowledge.config;

import com.lark.oapi.Client;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 飞书应用配置属性类
 * <p>用于从 application.yml 中读取飞书应用的配置信息（appId 和 appSecret），
 * 并创建飞书客户端 Bean 供其他组件使用。</p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "feishu.app")
public class FeishuAppProperties {

    /**
     * 飞书应用的 App ID
     * <p>在飞书开放平台创建应用后获得，用于身份认证</p>
     */
    private String appId;

    /**
     * 飞书应用的 App Secret
     * <p>在飞书开放平台创建应用后获得，用于签名验证</p>
     */
    private String appSecret;


    /**
     * 创建飞书官方客户端 Bean
     * <p>使用配置的 appId 和 appSecret 构建飞书客户端实例，
     * 该客户端可用于调用飞书开放平台的各类 API（如消息发送、用户管理等）。</p>
     *
     * @return 飞书客户端实例，已配置好 appId 和 appSecret
     */
    @Bean
    public Client feishuClient() {
        return Client.newBuilder(appId, appSecret).build();
    }


}

