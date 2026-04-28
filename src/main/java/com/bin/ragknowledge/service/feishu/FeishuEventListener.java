package com.bin.ragknowledge.service.feishu;

import com.bin.ragknowledge.service.RagService;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.service.im.ImService;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import com.lark.oapi.ws.Client;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeishuEventListener {

    private final FeishuMessageSender feishuMessageSender;
    private final RagService ragService;

    private Client wsClient;

    @PostConstruct
    public void init() {
        startListener();
    }

    public synchronized void restart() {
        log.info("重启飞书监听器...");
        stopListener();
        feishuMessageSender.refreshConfig();
        startListener();
    }

    private synchronized void startListener() {
        if (!feishuMessageSender.isEnabled()) {
            log.info("飞书机器人未启用，跳过初始化");
            return;
        }

        String appId = feishuMessageSender.getAppId();
        String appSecret = feishuMessageSender.getAppSecret();

        if (appId == null || appSecret == null || appId.isEmpty()) {
            log.warn("飞书 appId 或 appSecret 未配置");
            return;
        }

        try {
            EventDispatcher eventDispatcher = EventDispatcher.newBuilder("", "")
                    .onP2MessageReceiveV1(new ImService.P2MessageReceiveV1Handler() {
                        @Override
                        public void handle(P2MessageReceiveV1 event) {
                            log.info("[飞书消息接收], data: {}", Jsons.DEFAULT.toJson(event.getEvent()));
                            String text = event.getEvent().getMessage().getContent();
                            String messageId = event.getEvent().getMessage().getMessageId();

                            Thread.startVirtualThread(() -> {
                                processMessage(text, messageId);
                            });
                        }
                    })
                    .build();

            wsClient = new Client.Builder(appId, appSecret)
                    .eventHandler(eventDispatcher)
                    .build();
            wsClient.start();
            log.info("飞书 RAG Bot 启动成功");
        } catch (Exception e) {
            log.error("飞书监听器启动失败", e);
        }
    }

    private synchronized void stopListener() {
        if (wsClient != null) {
            wsClient = null;
            log.info("飞书客户端已关闭");
        }
    }

    public void processMessage(String rawContent, String messageId) {
        try {
            String question = feishuMessageSender.parseText(rawContent);
            if (question.isEmpty()) {
                return;
            }
            String answer = ragService.query(question);
            feishuMessageSender.sendRichText(messageId, "🤖 知识库智能助手", answer);
        } catch (Exception e) {
            log.error("处理飞书消息失败", e);
        }
    }
}