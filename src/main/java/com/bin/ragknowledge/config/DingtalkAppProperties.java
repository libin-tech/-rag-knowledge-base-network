package com.bin.ragknowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * 钉钉应用配置属性类
 * <p>用于从 application.yml 中读取钉钉应用的配置信息（appkey 和 appSecret），
 * 并创建钉钉客户端 Bean 供其他组件使用。</p>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dingtalk.app")
public class DingtalkAppProperties {

    /**
     * 钉钉应用的 App ID
     * <p>在钉钉开放平台创建应用后获得，用于身份认证</p>
     */
    private String appkey;

    /**
     * 钉钉应用的 App Secret
     * <p>在钉钉开放平台创建应用后获得，用于签名验证</p>
     */
    private String appSecret;





}

