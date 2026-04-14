package com.family.assistant.service;

import com.family.assistant.config.FeishuProperties;
import com.family.assistant.dto.FeishuMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 飞书集成服务
 * <p>
 * 职责：负责与飞书开放平台进行交互，实现飞书机器人消息的接收、处理和回复功能。
 * 该服务作为系统与飞书生态的桥梁，将飞书消息转换为内部问答流程，并将 AI 生成的
 * 回答通过飞书 API 回复给用户。
 * </p>
 *
 * <p>核心工作流程：</p>
 * <ol>
 *   <li>接收飞书推送的事件消息（通过 Webhook）</li>
 *   <li>验证请求来源的合法性（通过 verification_token）</li>
 *   <li>解析消息内容，提取用户问题</li>
 *   <li>调用 RAG 服务获取智能回答</li>
 *   <li>构建回复消息并通过飞书 API 发送给用户</li>
 * </ol>
 *
 * <p>依赖组件：</p>
 * <ul>
 *   <li>FeishuProperties：飞书应用配置（appId、appSecret、verificationToken）</li>
 *   <li>RagService：RAG 智能问答服务，负责生成问题答案</li>
 *   <li>RestTemplate：Spring HTTP 客户端，用于调用飞书开放平台 API</li>
 *   <li>ObjectMapper：JSON 序列化和反序列化工具</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeishuService {

    /** 飞书应用配置属性，包含 appId、appSecret 和 verificationToken */
    private final FeishuProperties feishuProperties;

    /** RAG 智能问答服务，用于生成问题的智能回答 */
    private final RagService ragService;

    /** Spring REST 模板，用于发起 HTTP 请求调用飞书开放平台 API */
    private final RestTemplate restTemplate = new RestTemplate();

    /** JSON 处理工具，用于解析和构建 JSON 格式的请求/响应数据 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理飞书消息事件
     * <p>
     * 工作流程：
     * 1. 从消息对象中提取事件信息和消息内容
     * 2. 校验消息类型，当前仅支持文本消息
     * 3. 解析消息内容，提取用户实际提出的问题
     * 4. 调用 RAG 服务进行智能问答
     * 5. 构建标准格式的回复消息并返回
     * 6. 异常处理：任何环节出错都返回友好的错误提示
     * </p>
     *
     * @param feishuMessage 飞书推送的完整消息对象，包含事件信息和消息内容
     * @return 符合飞书消息格式的响应 Map，包含消息类型和回复内容
     */
    public Map<String, Object> handleMessage(FeishuMessage feishuMessage) {
        try {
            // 从消息对象中获取事件数据
            FeishuMessage.Event event = feishuMessage.getEvent();
            FeishuMessage.Message message = event.getMessage();

            // 校验消息类型，当前版本仅支持处理纯文本消息
            // 对于图片、文件等其他类型的消息，返回不支持的提示
            if (!"text".equals(message.getMessageType())) {
                return buildTextResponse("暂不支持该消息类型");
            }

            // 解析消息内容，从 JSON 格式中提取用户实际输入的文本
            String question = parseQuestion(message.getContent());
            log.info("收到飞书用户提问: {}", question);

            // 调用 RAG 智能问答服务获取问题的答案
            String answer = ragService.query(question);

            // 构建标准格式的文本回复
            return buildTextResponse(answer);

        } catch (Exception e) {
            // 全局异常处理，确保任何错误都不会导致服务崩溃
            // 返回友好的错误提示，提升用户体验
            log.error("处理飞书消息失败", e);
            return buildTextResponse("抱歉，处理您的问题时出现了错误，请稍后再试");
        }
    }

    /**
     * 回复飞书消息
     * <p>
     * 工作流程：
     * 1. 构建飞书消息回复 API 的 URL
     * 2. 设置请求头，包含 Content-Type 和 Authorization（Bearer Token）
     * 3. 构建请求体，包含消息类型和回复内容
     * 4. 发起 PUT 请求发送回复消息
     * 5. 记录成功或失败的日志
     * </p>
     * <p>
     * 注意：此方法调用飞书开放平台的 "回复消息" API，
     * 需要有效的 tenant_access_token 进行鉴权。
     * </p>
     *
     * @param messageId 需要回复的飞书消息 ID，用于定位具体回复哪条消息
     * @param content   回复的消息内容文本
     */
    public void replyMessage(String messageId, String content) {
        try {
            // 构建飞书消息回复 API 的完整 URL
            String url = "https://open.feishu.cn/open-apis/im/v1/messages/" + messageId;

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);  // 指定请求内容为 JSON 格式
            // 设置鉴权 Token，注意：实际生产环境中需要缓存 token 避免频繁请求
            headers.set("Authorization", "Bearer " + getTenantAccessToken());

            // 构建请求体，符合飞书消息 API 的格式要求
            Map<String, Object> body = new HashMap<>();
            body.put("content", content);      // 消息内容
            body.put("msg_type", "text");      // 消息类型：文本

            // 封装请求实体
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            // 发起 PUT 请求，调用飞书 API 发送回复
            restTemplate.put(url, request);

            log.info("成功回复飞书消息: {}", messageId);
        } catch (Exception e) {
            // 记录回复失败的异常信息
            log.error("回复飞书消息失败", e);
        }
    }

    /**
     * 获取飞书 Tenant Access Token
     * <p>
     * 工作流程：
     * 1. 构建获取 Token 的 API 请求 URL
     * 2. 设置请求体，包含应用的 appId 和 appSecret
     * 3. 发起 POST 请求调用飞书鉴权 API
     * 4. 从响应中提取 tenant_access_token 字段
     * 5. 异常处理：失败时记录日志并返回空字符串
     * </p>
     * <p>
     * 重要提示：
     * - Token 有效期为 2 小时，过期后需要重新获取
     * - 生产环境应实现 Token 缓存机制（如使用 Spring Cache 或 Redis），
     *   避免每次请求都调用飞书鉴权 API，降低接口调用频率和网络开销
     * - 当前实现为 TODO 状态，标记了需要优化的缓存逻辑
     * </p>
     *
     * @return 飞书 tenant_access_token 字符串，获取失败时返回空字符串
     */
    private String getTenantAccessToken() {
        // TODO: 实际实现需要缓存 token，token 有效期为 2 小时
        try {
            // 飞书内部应用获取 tenant_access_token 的 API 端点
            String url = "https://open.feishu.cn/open-apis/auth/v3/tenant_access_token/internal";

            // 构建请求体，使用配置中的应用凭证
            Map<String, String> body = new HashMap<>();
            body.put("app_id", feishuProperties.getAppId());
            body.put("app_secret", feishuProperties.getAppSecret());

            // 封装请求实体
            HttpEntity<Map<String, String>> request = new HttpEntity<>(body);
            // 发起 POST 请求，响应体为 JSON 格式
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, request, JsonNode.class);

            // 从响应 JSON 中提取 tenant_access_token 字段
            if (response.getBody() != null) {
                return response.getBody().get("tenant_access_token").asText();
            }
        } catch (Exception e) {
            // 记录获取 Token 失败的异常信息
            log.error("获取 Tenant Access Token 失败", e);
        }
        // 发生异常或响应为空时返回空字符串
        return "";
    }

    /**
     * 解析飞书消息内容，提取用户实际输入的文本
     * <p>
     * 飞书的消息内容以 JSON 格式传输，结构通常为 {"text": "用户输入的内容"}。
     * 此方法负责从 JSON 中解析出 text 字段。
     * </p>
     *
     * @param content 飞书消息的原始 JSON 字符串内容
     * @return 解析后的用户问题文本，解析失败时返回原始内容作为降级策略
     */
    private String parseQuestion(String content) {
        try {
            // 使用 ObjectMapper 解析 JSON 字符串
            JsonNode jsonNode = objectMapper.readTree(content);
            // 提取 text 字段作为用户问题
            return jsonNode.get("text").asText();
        } catch (Exception e) {
            // 解析失败时记录日志，并返回原始内容作为降级处理
            // 这种策略确保即使 JSON 格式异常也能继续处理消息
            log.error("解析问题内容失败", e);
            return content;
        }
    }

    /**
     * 构建飞书文本回复消息
     * <p>
     * 按照飞书消息 API 的格式要求构建回复数据。
     * 飞书文本回复的格式为：{"msg_type": "text", "content": {"text": "回复内容"}}
     * </p>
     *
     * @param text 需要回复的文本内容
     * @return 符合飞书消息格式的 Map 对象，可直接用于 API 响应
     */
    private Map<String, Object> buildTextResponse(String text) {
        Map<String, Object> response = new HashMap<>();
        response.put("msg_type", "text");  // 指定消息类型为文本

        // 构建 content 嵌套对象
        Map<String, String> content = new HashMap<>();
        content.put("text", text);
        response.put("content", content);

        return response;
    }

    /**
     * 验证请求是否来自飞书开放平台
     * <p>
     * 飞书在发送事件回调时会携带 verification_token 参数，
     * 通过比对配置的 token 值来验证请求来源的合法性，
     * 防止恶意伪造请求。
     * </p>
     *
     * @param token 请求中携带的 verification_token 参数值
     * @return true 表示验证通过，请求来自飞书；false 表示验证失败
     */
    public boolean verifyRequest(String token) {
        // 将请求中的 token 与配置的 verificationToken 进行精确匹配
        return feishuProperties.getVerificationToken().equals(token);
    }
}
