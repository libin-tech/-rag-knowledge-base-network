package com.bin.ragknowledge.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * LLM配置键枚举
 * 用于统一管理LLM配置的各项配置键
 */
@AllArgsConstructor
@Getter
public enum LlmConfigKey {

    /** 模式配置键 */
    MODE("mode"),

    /** DashScope API密钥 */
    DASHSCOPE_API_KEY("dashscope_apiKey"),

    /** DashScope模型名称 */
    DASHSCOPE_MODEL_NAME("dashscope_modelName"),

    /** Ollama服务地址 */
    OLLAMA_BASE_URL("ollama_baseUrl"),

    /** Ollama模型名称 */
    OLLAMA_MODEL_NAME("ollama_modelName"),

    /** Ollama超时时间 */
    OLLAMA_TIMEOUT("ollama_timeout"),

    /** OpenAI API密钥 */
    OPENAI_API_KEY("openai_apiKey"),

    /** OpenAI服务地址 */
    OPENAI_BASE_URL("openai_baseUrl"),

    /** OpenAI模型名称 */
    OPENAI_MODEL_NAME("openai_modelName"),

    /** OpenAI超时时间 */
    OPENAI_TIMEOUT("openai_timeout");

    @JsonValue
    @EnumValue
    private final String value;


}
