package com.bintech.rag.service.dingtalk;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.bintech.rag.repository.entity.KnowledgeBaseEntity;
import com.bintech.rag.service.KnowledgeBaseService;
import com.dingtalk.open.app.api.OpenDingTalkClient;
import com.dingtalk.open.app.api.OpenDingTalkStreamClientBuilder;
import com.dingtalk.open.app.api.security.AuthClientCredential;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DingtalkEventListener {

    private final DingtalkMessageSender dingtalkMessageSender;
    private final KnowledgeBaseService knowledgeBaseService;
    private final Map<String, OpenDingTalkClient> clientMap = new ConcurrentHashMap<>();
    private static final String CALLBACK_ENDPOINT = "/v1.0/im/bot/messages/get";

    @PostConstruct
    public void init() {
        refreshAndStart();
    }

    public synchronized void restart() {
        refreshAndStart();
    }

    public synchronized void restartForKnowledgeBase(String knowledgeBaseId) {
        log.info("为知识库 {} 重启钉钉监听器...", knowledgeBaseId);
        stopListenerForKnowledgeBase(knowledgeBaseId);
        startListenerForKnowledgeBase(knowledgeBaseId);
    }

    public synchronized void refreshAndStart() {
        stopAllListeners();
        List<KnowledgeBaseEntity> enabledKnowledgeBases = knowledgeBaseService.listEnabled();
        log.info("开始启动钉钉监听器, 启用知识库数量: {}", enabledKnowledgeBases.size());
        
        for (KnowledgeBaseEntity kb : enabledKnowledgeBases) {
            startListenerForKnowledgeBase(kb.getId());
        }
        
        log.info("钉钉监听器启动完成, 已启动知识库数量: {}", clientMap.size());
    }

    private synchronized void startListenerForKnowledgeBase(String knowledgeBaseId) {
        dingtalkMessageSender.refreshConfig(knowledgeBaseId);
        
        if (!dingtalkMessageSender.isEnabled()) {
            log.info("钉钉机器人未启用，跳过知识库 {}", knowledgeBaseId);
            return;
        }

        String clientId = dingtalkMessageSender.getClientId();
        String clientSecret = dingtalkMessageSender.getClientSecret();

        if (clientId == null || clientSecret == null || clientId.isEmpty()) {
            log.warn("钉钉 clientId 或 clientSecret 未配置，跳过知识库 {}", knowledgeBaseId);
            return;
        }

        try {
            OpenDingTalkClient client = OpenDingTalkStreamClientBuilder.custom()
                    .credential(new AuthClientCredential(clientId, clientSecret))
                    .registerCallbackListener(CALLBACK_ENDPOINT, dingtalkMessageSender)
                    .build();

            client.start();
            clientMap.put(knowledgeBaseId, client);
            log.info("钉钉 RAG Bot 启动成功, 关联知识库: {}", knowledgeBaseId);
        } catch (Exception e) {
            log.error("钉钉监听器启动失败, 知识库: {}", knowledgeBaseId, e);
        }
    }

    private synchronized void stopListenerForKnowledgeBase(String knowledgeBaseId) {
        try {
            OpenDingTalkClient client = clientMap.remove(knowledgeBaseId);
            if (client != null) {
                client.stop();
                log.info("钉钉 RAG Bot 已关闭, 知识库: {}", knowledgeBaseId);
            }
        } catch (Exception e) {
            log.error("关闭钉钉 RAG Bot 时发生错误, 知识库: {}", knowledgeBaseId, e);
        }
    }

    private synchronized void stopAllListeners() {
        for (String knowledgeBaseId : clientMap.keySet()) {
            stopListenerForKnowledgeBase(knowledgeBaseId);
        }
        clientMap.clear();
    }
}
