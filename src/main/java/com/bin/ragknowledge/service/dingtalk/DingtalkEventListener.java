package com.bin.ragknowledge.service.dingtalk;

import com.dingtalk.open.app.api.OpenDingTalkClient;
import com.dingtalk.open.app.api.OpenDingTalkStreamClientBuilder;
import com.dingtalk.open.app.api.security.AuthClientCredential;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DingtalkEventListener  {

    private final DingtalkMessageSender dingtalkMessageSender;
    private OpenDingTalkClient client;
    private static final String CALLBACK_ENDPOINT = "/v1.0/im/bot/messages/get";

    @PostConstruct
    public void init() {
        startListener();
    }

    public synchronized void restart() {
        log.info("重启钉钉监听器...");
        stopListener();
        dingtalkMessageSender.refreshConfig();
        startListener();
    }

    private synchronized void startListener() {
        if (!dingtalkMessageSender.isEnabled()) {
            log.info("钉钉机器人未启用，跳过初始化");
            return;
        }

        String clientId = dingtalkMessageSender.getClientId();
        String clientSecret = dingtalkMessageSender.getClientSecret();

        if (clientId == null || clientSecret == null || clientId.isEmpty()) {
            log.warn("钉钉 clientId 或 clientSecret 未配置");
            return;
        }

        try {
            client = OpenDingTalkStreamClientBuilder.custom()
                    .credential(new AuthClientCredential(clientId, clientSecret))
                    .registerCallbackListener(CALLBACK_ENDPOINT, dingtalkMessageSender)
                    .build();

            client.start();
            log.info("钉钉 RAG Bot 启动成功");
        } catch (Exception e) {
            log.error("钉钉监听器启动失败", e);
        }
    }

    private synchronized void stopListener() {

        try {
            if (client != null) {
                client.stop();
                client = null;
                log.info("钉钉 RAG Bot 已关闭");
            }
        } catch (Exception e) {
            log.error("关闭钉钉 RAG Bot 时发生错误", e);
        }
    }




}