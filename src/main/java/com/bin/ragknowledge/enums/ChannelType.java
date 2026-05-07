package com.bin.ragknowledge.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 渠道类型枚举
 * 支持飞书、钉钉、企业微信三种渠道
 */
@AllArgsConstructor
@Getter
public enum ChannelType {

    /**
     * 飞书渠道
     */
    FEISHU("FEISHU", "飞书"),

    /**
     * 钉钉渠道
     */
    DINGTALK("DINGTALK", "钉钉"),

    /**
     * 企业微信渠道
     */
    WECHAT_WORK("WECHAT_WORK", "企业微信");

    @JsonValue
    @EnumValue
    private final String code;
    private final String desc;


}