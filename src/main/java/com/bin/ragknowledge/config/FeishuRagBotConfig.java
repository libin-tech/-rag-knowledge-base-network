package com.bin.ragknowledge.config;

import com.bin.ragknowledge.service.RagService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lark.oapi.core.utils.Jsons;
import com.lark.oapi.event.EventDispatcher;
import com.lark.oapi.service.im.ImService;
import com.lark.oapi.service.im.v1.model.P2MessageReceiveV1;
import com.lark.oapi.service.im.v1.model.ReplyMessageReq;
import com.lark.oapi.service.im.v1.model.ReplyMessageReqBody;
import com.lark.oapi.ws.Client;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FeishuRagBotConfig {


    private final FeishuAppProperties feishuAppProperties;
    private final RagService ragService;
    private final com.lark.oapi.Client feishuClient;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @PostConstruct
    public void init() throws Exception {
        // 1. 定义事件处理器
        EventDispatcher eventDispatcher = EventDispatcher.newBuilder("", "")
                .onP2MessageReceiveV1(new ImService.P2MessageReceiveV1Handler() {
                    @Override
                    public void handle(P2MessageReceiveV1 event) {

                        log.info("[onP2MessageReceiveV1 access ], data: {}", Jsons.DEFAULT.toJson(event.getEvent()));
                        // 获取消息内容
                        String text = event.getEvent().getMessage().getContent(); // 注意：飞书内容是JSON字符串
                        String messageId = event.getEvent().getMessage().getMessageId();

                        // 使用 Java 21 虚拟线程异步处理 RAG
                        Thread.startVirtualThread(() -> {
                            processRagTask(text, messageId);
                        });
                    }
                })
                .build();

        // 2. 启动长连接 (WebSocket)
        Client wsClient = new Client.Builder(feishuAppProperties.getAppId(), feishuAppProperties.getAppSecret())
                .eventHandler(eventDispatcher)
                .build();
        wsClient.start();
        log.info("飞书 RAG Bot 启动成功");
    }

    private void processRagTask(String rawContent, String messageId) {
        try {
            // 飞书文本消息是 JSON 格式: {"text":"问题内容"}
            String question = parseText(rawContent);

            // 执行 RAG 检索
            String answer = ragService.query(question);

            // 回复卡片消息
            replyCard(messageId, answer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析飞书消息内容
     *
     * @param rawContent 飞书传来的原始 JSON 字符串, 如 {"text":"问题内容"}
     * @return 纯文本内容
     */
    public String parseText(String rawContent) {
        try {
            // 1. 将字符串解析为 JSON 对象
            JsonNode node = objectMapper.readTree(rawContent);

            // 2. 飞书文本消息的 key 是 "text"
            if (node.has("text")) {
                String originalText = node.get("text").asText();

                // 3. 过滤掉飞书的 @ 标签 (例如: <at user_id="xxx">Name</at>)
                // 如果不清理，这些标签会影响向量检索的准确度
                String cleanText = originalText.replaceAll("<at [^>]*>[^<]*</at>", "");

                return cleanText.trim();
            }
        } catch (Exception e) {
            log.error("解析飞书消息失败: rawContent={}", rawContent, e);
        }
        return "";
    }

    /**
     * 回复用户消息 普通文本模式
     *
     * @param messageId 触发事件的原消息ID
     * @param content   RAG系统生成的内容
     * @throws Exception
     */
    public void reply(String messageId, String content) throws Exception {
        // 构建回复内容（Markdown 格式）
        // 飞书 Markdown 语法：https://open.feishu.cn/document/uAjLw4CM/ukTMukTMukTM/im-v1/message/create_json
        String replyContent = "{\"text\":\"" + escapeJson(content) + "\"}";

        ReplyMessageReq req = ReplyMessageReq.newBuilder()
                .messageId(messageId)
                .replyMessageReqBody(ReplyMessageReqBody.newBuilder()
                        .msgType("text") // 或者使用 "post" 发送富文本
                        .content(replyContent)
                        .build())
                .build();

        feishuClient.im().message().reply(req);
    }

    private String escapeJson(String str) {
        return str.replace("\n", "\\n").replace("\"", "\\\"");
    }


    /**
     * 回复用户消息（卡片模式）
     *
     * @param messageId       触发事件的原消息ID
     * @param markdownContent RAG系统生成的Markdown内容
     */
    public void replyCard(String messageId, String markdownContent) throws Exception {

        // 1. 使用 Jackson 动态构建卡片 JSON，防止手动拼接 JSON 导致转义失败
        ObjectNode cardNode = objectMapper.createObjectNode();

        // config 部分
        ObjectNode config = objectMapper.createObjectNode();
        config.put("wide_screen_mode", true);
        cardNode.set("config", config);

        // header 部分
        ObjectNode header = objectMapper.createObjectNode();
        header.put("template", "blue"); // 蓝色标题头
        ObjectNode title = objectMapper.createObjectNode();
        title.put("tag", "plain_text");
        title.put("content", "🤖 知识库智能助手");
        header.set("title", title);
        cardNode.set("header", header);

        // elements 部分
        ArrayNode elements = objectMapper.createArrayNode();

        // 主体 Markdown 内容
        ObjectNode mdElement = objectMapper.createObjectNode();
        mdElement.put("tag", "markdown");
        mdElement.put("content", markdownContent);
        elements.add(mdElement);

        // 分割线
        ObjectNode hrElement = objectMapper.createObjectNode();
        hrElement.put("tag", "hr");
        elements.add(hrElement);

        // 备注
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

        // 2. 调用飞书 API 回复卡片消息
        ReplyMessageReq req = ReplyMessageReq.newBuilder()
                .messageId(messageId)
                .replyMessageReqBody(ReplyMessageReqBody.newBuilder()
                        .msgType("interactive") // 必须是 interactive 类型
                        .content(cardJsonString) // 卡片 JSON 字符串
                        .build())
                .build();

        feishuClient.im().message().reply(req);
    }


}
