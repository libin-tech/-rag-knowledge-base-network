package com.bintech.rag.service.feishu;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSONObject;
import com.bintech.rag.enums.ChannelType;
import com.bintech.rag.service.MessageChannelService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lark.oapi.Client;
import com.lark.oapi.service.im.v1.model.ReplyMessageReq;
import com.lark.oapi.service.im.v1.model.ReplyMessageReqBody;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeishuMessageSender {

    private final MessageChannelService messageChannelService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Client client;
    @Getter
    private String appId;
    @Getter
    private String appSecret;
    @Getter
    private volatile boolean enabled = false;
    @Getter
    private String knowledgeBaseId = "default";

    @PostConstruct
    public void init() {
        refreshConfig();
    }

    public void refreshConfig() {
        refreshConfig("default");
    }

    public void refreshConfig(String knowledgeBaseId) {
        var channel = messageChannelService.getByTypeAndKb(ChannelType.FEISHU, knowledgeBaseId);
        if (channel != null) {
            enabled = channel.getEnabled();
            this.knowledgeBaseId = channel.getKnowledgeBaseId() != null ? channel.getKnowledgeBaseId() : knowledgeBaseId;
            if (channel.getConfigJson() != null) {
                JSONObject config = JSONObject.parseObject(channel.getConfigJson());
                appId = config.getString("appId");
                appSecret = config.getString("appSecret");
                if (appId != null && appSecret != null && !appId.isEmpty()) {
                    client = Client.newBuilder(appId, appSecret).build();
                    log.info("飞书客户端初始化完成, enabled: {}, knowledgeBaseId: {}", enabled, this.knowledgeBaseId);
                }
            }
        }
    }



    public void sendText(String userId, String content) {
        if (!enabled || client == null) {
            log.warn("飞书未启用或未配置");
            return;
        }
        String replyContent = "{\"text\":\"" + escapeJson(content) + "\"}";
        try {
            ReplyMessageReq req = ReplyMessageReq.newBuilder()
                    .messageId(userId)
                    .replyMessageReqBody(ReplyMessageReqBody.newBuilder()
                            .msgType("text")
                            .content(replyContent)
                            .build())
                    .build();
            client.im().message().reply(req);
        } catch (Exception e) {
            log.error("飞书消息发送失败", e);
        }
    }

    public void sendRichText(String userId, String title, String content) {
        if (!enabled || client == null) {
            log.warn("飞书未启用或未配置");
            return;
        }
        try {
            sendRichCard(userId, title, content);
        } catch (Exception e) {
            log.error("飞书富文本消息发送失败", e);
        }
    }


    private void sendRichCard(String messageId, String title, String markdownContent) throws Exception {
        ObjectNode cardNode = objectMapper.createObjectNode();

        ObjectNode config = objectMapper.createObjectNode();
        config.put("wide_screen_mode", true);
        cardNode.set("config", config);

        ObjectNode header = objectMapper.createObjectNode();
        header.put("template", "blue");
        ObjectNode titleNode = objectMapper.createObjectNode();
        titleNode.put("tag", "plain_text");
        titleNode.put("content", title != null ? title : "🤖 知识库智能助手");
        header.set("title", titleNode);
        cardNode.set("header", header);

        ArrayNode elements = objectMapper.createArrayNode();

        ObjectNode mdElement = objectMapper.createObjectNode();
        mdElement.put("tag", "markdown");
        mdElement.put("content", markdownContent);
        elements.add(mdElement);

        ObjectNode hrElement = objectMapper.createObjectNode();
        hrElement.put("tag", "hr");
        elements.add(hrElement);

        ObjectNode noteElement = objectMapper.createObjectNode();
        noteElement.put("tag", "note");
        ArrayNode noteElements = objectMapper.createArrayNode();
        ObjectNode noteText = objectMapper.createObjectNode();
        noteText.put("tag", "plain_text");
        noteText.put("content", "💡 以上内容基于企业知识库检索生成");
        noteElements.add(noteText);
        noteElement.set("elements", noteElements);
        elements.add(noteElement);

        cardNode.set("elements", elements);

        String cardJsonString = objectMapper.writeValueAsString(cardNode);

        ReplyMessageReq req = ReplyMessageReq.newBuilder()
                .messageId(messageId)
                .replyMessageReqBody(ReplyMessageReqBody.newBuilder()
                        .msgType("interactive")
                        .content(cardJsonString)
                        .build())
                .build();

        client.im().message().reply(req);
    }

    private String escapeJson(String str) {
        return str.replace("\n", "\\n").replace("\"", "\\\"");
    }

    public String parseText(String rawContent) {
        try {
            JsonNode node = objectMapper.readTree(rawContent);
            if (node.has("text")) {
                String originalText = node.get("text").asText();
                return originalText.replaceAll("<at[^>]*>[^<]*</at>", "").trim();
            }
        } catch (Exception e) {
            log.error("解析飞书消息失败", e);
        }
        return "";
    }

}