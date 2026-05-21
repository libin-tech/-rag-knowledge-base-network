package com.bintech.rag.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.bintech.rag.enums.ChannelType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_channel")
public class MessageChannelEntity extends BaseEntity {

    private ChannelType channelType;

    private String channelName;

    private Boolean enabled;

    private String configJson;

    private String remark;

    /**
     * 知识库ID
     * <p>
     * 消息渠道关联的知识库标识，机器人提问时在此知识库中检索。
     * </p>
     */
    private String knowledgeBaseId;
}