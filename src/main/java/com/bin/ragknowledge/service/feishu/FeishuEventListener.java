package com.bin.ragknowledge.service.feishu;

import com.bin.ragknowledge.repository.entity.KnowledgeBaseEntity;
import com.bin.ragknowledge.service.KnowledgeBaseService;
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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeishuEventListener {

    private final FeishuMessageSender feishuMessageSender;
    private final RagService ragService;
    private final KnowledgeBaseService knowledgeBaseService;
    private final Map<String, Client> clientMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refreshAndStart();
    }

    public synchronized void restart() {
        refreshAndStart();
    }

    public synchronized void restartForKnowledgeBase(String knowledgeBaseId) {
        log.info("为知识库 {} 重启飞书监听器...", knowledgeBaseId);
        stopListenerForKnowledgeBase(knowledgeBaseId);
        startListenerForKnowledgeBase(knowledgeBaseId);
    }

    public synchronized void refreshAndStart() {
        stopAllListeners();
        List<KnowledgeBaseEntity> enabledKnowledgeBases = knowledgeBaseService.listEnabled();
        log.info("开始启动飞书监听器, 启用知识库数量: {}", enabledKnowledgeBases.size());
        
        for (KnowledgeBaseEntity kb : enabledKnowledgeBases) {
            startListenerForKnowledgeBase(kb.getId());
        }
        
        log.info("飞书监听器启动完成, 已启动知识库数量: {}", clientMap.size());
    }

    private synchronized void startListenerForKnowledgeBase(String knowledgeBaseId) {
        feishuMessageSender.refreshConfig(knowledgeBaseId);
        
        String appId = feishuMessageSender.getAppId();
        String appSecret = feishuMessageSender.getAppSecret();

        if (!feishuMessageSender.isEnabled()) {
            log.info("飞书机器人未启用，跳过知识库 {}", knowledgeBaseId);
            return;
        }

        if (appId == null || appSecret == null || appId.isEmpty()) {
            log.warn("飞书 appId 或 appSecret 未配置，跳过知识库 {}", knowledgeBaseId);
            return;
        }

        try {
            String finalKnowledgeBaseId = knowledgeBaseId;
            EventDispatcher eventDispatcher = EventDispatcher.newBuilder("", "")
                    .onP2MessageReceiveV1(new ImService.P2MessageReceiveV1Handler() {
                        @Override
                        public void handle(P2MessageReceiveV1 event) {
                            log.info("[飞书消息接收], knowledgeBaseId: {}, data: {}", finalKnowledgeBaseId, Jsons.DEFAULT.toJson(event.getEvent()));
                            String text = event.getEvent().getMessage().getContent();
                            String messageId = event.getEvent().getMessage().getMessageId();

                            Thread.startVirtualThread(() -> {
                                processMessage(text, messageId, finalKnowledgeBaseId);
                            });
                        }
                    })
                    .build();

            Client wsClient = new Client.Builder(appId, appSecret)
                    .eventHandler(eventDispatcher)
                    .build();
            wsClient.start();
            clientMap.put(knowledgeBaseId, wsClient);
            log.info("飞书 RAG Bot 启动成功, 关联知识库: {}", knowledgeBaseId);
        } catch (Exception e) {
            log.error("飞书监听器启动失败, 知识库: {}", knowledgeBaseId, e);
        }
    }

    private synchronized void stopListenerForKnowledgeBase(String knowledgeBaseId) {
        Client wsClient = clientMap.remove(knowledgeBaseId);
        if (wsClient != null) {
            log.info("飞书客户端已关闭, 知识库: {}", knowledgeBaseId);
        }
    }

    private synchronized void stopAllListeners() {
        for (String knowledgeBaseId : clientMap.keySet()) {
            stopListenerForKnowledgeBase(knowledgeBaseId);
        }
        clientMap.clear();
    }

    public void processMessage(String rawContent, String messageId, String knowledgeBaseId) {
        try {
            String question = feishuMessageSender.parseText(rawContent);
            if (question.isEmpty()) {
                return;
            }
            String answer = ragService.query(question, knowledgeBaseId);
            feishuMessageSender.sendRichText(messageId, "🤖 知识库智能助手", answer);
        } catch (Exception e) {
            log.error("处理飞书消息失败, knowledgeBaseId: {}", knowledgeBaseId, e);
        }
    }
}