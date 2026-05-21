package com.bintech.rag.service.dingtalk;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenRequest;
import com.aliyun.dingtalkoauth2_1_0.models.GetAccessTokenResponse;
import com.aliyun.dingtalkrobot_1_0.Client;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOHeaders;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTORequest;
import com.aliyun.dingtalkrobot_1_0.models.BatchSendOTOResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.bintech.rag.enums.ChannelType;
import com.bintech.rag.service.MessageChannelService;
import com.bintech.rag.service.RagService;
import com.dingtalk.open.app.api.callback.OpenDingTalkCallbackListener;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class DingtalkMessageSender implements OpenDingTalkCallbackListener<JSONObject, JSONObject> {

    private final MessageChannelService messageChannelService;
    private final RagService ragService;

    @Getter
    private String clientId;
    @Getter
    private String clientSecret;

    @Getter
    private volatile boolean enabled = false;
    @Getter
    private String knowledgeBaseId = "default";
    private static final String PROTOCOL_HTTPS = "https";
    private static final String REGION_CENTRAL = "central";
    private static final long TOKEN_EXPIRY_SECONDS = 7000; // Token

    // Token缓存：存储access_token和过期时间戳
    private final ConcurrentHashMap<String, TokenCache> tokenCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refreshConfig();
    }

    public void refreshConfig() {
        refreshConfig("default");
    }

    public void refreshConfig(String knowledgeBaseId) {
        var channel = messageChannelService.getByTypeAndKb(ChannelType.DINGTALK, knowledgeBaseId);
        if (channel != null) {
            enabled = channel.getEnabled();
            this.knowledgeBaseId = channel.getKnowledgeBaseId() != null ? channel.getKnowledgeBaseId() : knowledgeBaseId;
            if (channel.getConfigJson() != null) {
                JSONObject config = JSONObject.parseObject(channel.getConfigJson());
                clientId = config.getString("clientId");
                clientSecret = config.getString("clientSecret");

                log.info("钉钉配置已加载, enabled: {}, clientId: {}, knowledgeBaseId: {}",
                        enabled, clientId != null ? "已设置" : "空", this.knowledgeBaseId);
            }
        }
    }


    /**
     * 获取缓存的Access Token，如果不存在或已过期则重新获取
     */
    private String getCachedToken(String appKey, String appSecret) {
        String cacheKey = appKey + ":" + appSecret;
        TokenCache cache = tokenCache.get(cacheKey);
        long currentTime = System.currentTimeMillis();

        if (cache != null && !cache.isExpired(currentTime)) {
            return cache.token();
        }

        // 获取新Token并缓存
        String newToken = fetchNewToken(appKey, appSecret);
        tokenCache.put(cacheKey, new TokenCache(newToken, currentTime + TOKEN_EXPIRY_SECONDS * 1000));
        return newToken;
    }

    /**
     * 从钉钉服务器获取新的Access Token
     */
    private String fetchNewToken(String clientId, String clientSecret) {
        GetAccessTokenRequest getAccessTokenRequest = new GetAccessTokenRequest();
        getAccessTokenRequest.setAppKey(clientId);
        getAccessTokenRequest.setAppSecret(clientSecret);

        try {
            // 初始化复用的Client实例
            Config config = new Config();
            config.protocol = PROTOCOL_HTTPS;
            config.regionId = REGION_CENTRAL;
            com.aliyun.dingtalkoauth2_1_0.Client oauthClient = new com.aliyun.dingtalkoauth2_1_0.Client(config);
            GetAccessTokenResponse accessToken = oauthClient.getAccessToken(getAccessTokenRequest);
            if (accessToken == null || accessToken.getBody() == null || accessToken.getBody().getAccessToken() == null) {
                log.error("获取钉钉Access Token失败，响应为空");
                throw new IllegalStateException("获取钉钉Access Token失败");
            }
            return accessToken.getBody().getAccessToken();
        } catch (Exception e) {
            log.error("获取钉钉Access Token时发生异常，clientId={}", clientId, e);
            throw new RuntimeException("获取钉钉Access Token失败", e);
        }
    }

    /**
     * Token缓存类
     */
    private record TokenCache(String token, long expiryTimestamp) {

        boolean isExpired(long currentTime) {
            return currentTime >= expiryTimestamp;
        }
    }


    @Override
    public JSONObject execute(JSONObject request) {
        // 参数校验
        if (request == null) {
            log.error("收到空的钉钉回调请求");
            return buildErrorResponse("请求参数为空");
        }

        Object senderStaffId = request.get("senderStaffId");
        com.alibaba.fastjson2.JSONObject textObj = request.getJSONObject("text");
        Object robotCodeObj = request.get("robotCode");

        if (senderStaffId == null || textObj == null || robotCodeObj == null) {
            log.error("钉钉回调请求参数不完整: senderStaffId={}, text={}, robotCode={}",
                    senderStaffId, textObj, robotCodeObj);
            return buildErrorResponse("请求参数不完整");
        }

        String userId = senderStaffId.toString();
        String content = textObj.getString("content");
        String robotCode = robotCodeObj.toString();

        if (content == null || content.isEmpty()) {
            log.warn("收到空消息内容，userId={}", userId);
            return buildErrorResponse("消息内容为空");
        }

        log.info("receive bot message from user={}, msg={},robotCode={} ", userId, content, robotCode);

        BatchSendOTOHeaders batchSendOTOHeaders = new BatchSendOTOHeaders();
        batchSendOTOHeaders.setXAcsDingtalkAccessToken(getCachedToken(clientId, clientSecret));

        BatchSendOTORequest batchSendOTORequest = new BatchSendOTORequest();
        batchSendOTORequest.setMsgKey("sampleMarkdown");
        batchSendOTORequest.setRobotCode(robotCode);
        batchSendOTORequest.setUserIds(Collections.singletonList(userId));

        com.alibaba.fastjson2.JSONObject msgParam = new com.alibaba.fastjson2.JSONObject();

        // 执行 RAG 检索（使用关联的知识库）
        String answer = ragService.query(content, knowledgeBaseId);

        msgParam.put("title", "🤖 知识库智能助手");
        msgParam.put("text", answer);
        batchSendOTORequest.setMsgParam(msgParam.toJSONString());

        try {
            // 初始化复用的Client实例
            Config config = new Config();
            config.protocol = PROTOCOL_HTTPS;
            config.regionId = REGION_CENTRAL;
            Client robotClient = new Client(config);
            BatchSendOTOResponse batchSendOTOResponse = robotClient.batchSendOTOWithOptions(
                    batchSendOTORequest, batchSendOTOHeaders, new RuntimeOptions());

            if (Objects.isNull(batchSendOTOResponse) || Objects.isNull(batchSendOTOResponse.getBody())) {
                log.error("RobotPrivateMessages_send batchSendOTOResponse return error, response={}, userId={}, robotCode={}",
                        batchSendOTOResponse, userId, robotCode);
                return buildErrorResponse("发送消息失败");
            }

            log.info("消息发送成功，userId={}, robotCode={}", userId, robotCode);
            return new com.alibaba.fastjson2.JSONObject();
        } catch (TeaException e) {
            log.error("RobotPrivateMessages_send batchSendOTOResponse throw TeaException, errCode={}, errorMessage={}, userId={}, robotCode={}",
                    e.getCode(), e.getMessage(), userId, robotCode, e);
            return buildErrorResponse("钉钉API调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("RobotPrivateMessages_send batchSendOTOResponse throw Exception, userId={}, robotCode={}",
                    userId, robotCode, e);
            return buildErrorResponse("系统异常: " + e.getMessage());
        }
    }

    /**
     * 构建错误响应
     */
    private JSONObject buildErrorResponse(String errorMsg) {
        JSONObject response = new JSONObject();
        response.put("success", false);
        response.put("error", errorMsg);
        return response;
    }
}