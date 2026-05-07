package com.bin.ragknowledge.config;

import com.bin.ragknowledge.enums.EmbeddingMode;
import lombok.Data;

/**
 * 向量嵌入配置类
 * 用于存储向量嵌入模型的所有配置参数，支持DashScope、Ollama、OpenAI三种模式
 */
@Data
public class EmbeddingConfig {

    /**
     * 向量嵌入模式：dashscope/ollama/openai
     */
    private String mode = EmbeddingMode.DASHSCOPE.getValue();

    /**
     * DashScope API密钥
     */
    private String dashscopeApiKey;

    /**
     * DashScope模型名称
     */
    private String dashscopeModelName = "text-embedding-v3";

    /**
     * Ollama服务地址
     */
    private String ollamaBaseUrl = "http://localhost:11434";

    /**
     * Ollama模型名称
     */
    private String ollamaModelName = "nomic-embed-text";

    /**
     * Ollama超时时间
     */
    private String ollamaTimeout = "60s";

    /**
     * OpenAI API密钥
     */
    private String openaiApiKey;

    /**
     * OpenAI服务地址
     */
    private String openaiBaseUrl = "https://api.openai.com";

    /**
     * OpenAI模型名称
     */
    private String openaiModelName = "text-embedding-3-small";

    /**
     * OpenAI超时时间
     */
    private String openaiTimeout = "120s";
}