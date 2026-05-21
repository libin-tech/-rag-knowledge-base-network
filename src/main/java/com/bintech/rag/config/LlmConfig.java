package com.bintech.rag.config;

import com.bintech.rag.enums.LlmMode;
import lombok.Data;

/**
 * LLM配置类
 * 用于存储LLM模型的所有配置参数，支持DashScope、Ollama、OpenAI三种模式
 */
@Data
public class LlmConfig {

    /**
     * LLM模式：dashscope/ollama/openai
     */
    private String mode = LlmMode.DASHSCOPE.getValue();

    /**
     * DashScope API密钥
     */
    private String dashscopeApiKey;

    /**
     * DashScope模型名称
     */
    private String dashscopeModelName = "qwen-plus";

    /**
     * Ollama服务地址
     */
    private String ollamaBaseUrl = "http://localhost:11434";

    /**
     * Ollama模型名称
     */
    private String ollamaModelName = "qwen3";

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
    private String openaiModelName = "gpt-4o-mini";

    /**
     * OpenAI超时时间
     */
    private String openaiTimeout = "120s";
}