package com.bin.ragknowledge.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("message_channel")
public class MessageChannelEntity extends BaseEntity {

    private String channelType;

    private String channelName;

    private Boolean enabled;

    private String configJson;

    private String remark;
}