package com.family.assistant.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 飞书消息数据传输对象（DTO）
 * 用于接收和解析飞书推送的消息事件数据结构
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeishuMessage {

    /** 消息协议版本，标识消息的结构版本 */
    private String schema;

    /** 消息头信息，包含事件ID、事件类型、创建时间等元数据 */
    @JsonProperty("header")
    private Header header;

    /** 事件具体内容，包含发送者、消息内容、聊天信息等 */
    @JsonProperty("event")
    private Event event;

    /**
     * 消息头内部类
     * 包含事件的元数据信息，如事件ID、事件类型、应用信息等
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        /** 事件唯一标识ID，用于去重和幂等处理 */
        @JsonProperty("event_id")
        private String eventId;

        /** 事件类型，标识具体发生的事件类型（如消息接收、群聊变更等） */
        @JsonProperty("event_type")
        private String eventType;

        /** 事件创建时间戳 */
        @JsonProperty("create_time")
        private String createTime;

        /** 事件验证令牌，用于验证事件的合法性 */
        @JsonProperty("token")
        private String token;

        /** 应用ID，标识触发该事件的应用 */
        @JsonProperty("app_id")
        private String appId;

        /** 租户密钥，标识事件所属的租户 */
        @JsonProperty("tenant_key")
        private String tenantKey;

        /** 资源ID，标识事件关联的资源 */
        @JsonProperty("resource_id")
        private String resourceId;

        /** 用户列表ID，用于批量用户相关事件 */
        @JsonProperty("user_list_id")
        private String userListId;
    }

    /**
     * 事件内容内部类
     * 包含具体的事件数据，如消息发送者、消息内容、聊天信息等
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Event {
        /** 消息发送者标识 */
        private String sender;

        /** 事件验证令牌 */
        private String token;

        /** 消息类型，如文本、图片、文件等 */
        private String message_type;

        /** 具体的消息内容对象 */
        private Message message;

        /** 聊天室ID，标识消息发送的群组或会话 */
        private String chat_id;

        /** 聊天类型，如群聊、单聊等 */
        private String chat_type;

        /** 操作者标识，用于标识执行操作的用户 */
        private String operator;

        /** 开放的聊天室ID */
        private String open_chat_id;

        /** 外部聊天室ID，用于跨组织的群聊 */
        private String external_chat_id;

        /** 用户的开放ID，用于标识用户身份 */
        @JsonProperty("open_id")
        private String openId;

        /** 用户ID，飞书内部用户标识 */
        @JsonProperty("user_id")
        private String userId;

        /** 联合ID，用于跨应用的用户身份标识 */
        @JsonProperty("union_id")
        private String unionId;
    }

    /**
     * 消息内容内部类
     * 包含具体的消息详细信息，如消息ID、内容、时间等
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        /** 消息唯一标识ID */
        @JsonProperty("message_id")
        private String messageId;

        /** 根消息ID，用于标识消息线程的根消息 */
        @JsonProperty("root_id")
        private String rootId;

        /** 父消息ID，用于标识消息的回复关系 */
        @JsonProperty("parent_id")
        private String parentId;

        /** 消息创建时间戳 */
        @JsonProperty("create_time")
        private String createTime;

        /** 消息所属的聊天室ID */
        @JsonProperty("chat_id")
        private String chatId;

        /** 消息所属的聊天类型 */
        @JsonProperty("chat_type")
        private String chatType;

        /** 消息类型，如 text、image、file 等 */
        @JsonProperty("message_type")
        private String messageType;

        /** 消息内容，JSON格式的字符串，具体结构取决于消息类型 */
        @JsonProperty("content")
        private String content;

        /** 提及的用户列表，包含被@的用户信息 */
        private List<String> mentions;
    }
}
