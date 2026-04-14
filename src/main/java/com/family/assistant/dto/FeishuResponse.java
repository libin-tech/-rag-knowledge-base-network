package com.family.assistant.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 飞书响应数据传输对象（DTO）
 * 用于封装向飞书返回的响应数据结构
 * 遵循飞书开放平台的响应格式规范
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeishuResponse {

    /** 响应状态码，0 表示成功，非 0 表示错误 */
    private Integer code;

    /** 响应消息，成功时为 "success"，错误时包含错误信息 */
    private String msg;

    /** 响应数据，为键值对形式的附加数据 */
    private Map<String, Object> data;
}
