package com.bintech.rag.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Embedding模式枚举
 * 支持DashScope、Ollama、OpenAI三种Embedding模型
 */
@AllArgsConstructor
@Getter
public enum EmbeddingMode {

    /** DashScope模式 */
    DASHSCOPE("dashscope"),

    /** Ollama模式 */
    OLLAMA("ollama"),

    /** OpenAI模式 */
    OPENAI("openai");

    @JsonValue
    @EnumValue
    private final String value;


}
