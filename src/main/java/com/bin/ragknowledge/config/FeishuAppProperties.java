package com.bin.ragknowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Data
@Configuration
@ConfigurationProperties(prefix = "feishu.app")
public class FeishuAppProperties {


    private String appId;
    private String appSecret;




    @Bean
    public com.lark.oapi.Client feishuClient() {
        return com.lark.oapi.Client.newBuilder(appId, appSecret).build();
    }



}
