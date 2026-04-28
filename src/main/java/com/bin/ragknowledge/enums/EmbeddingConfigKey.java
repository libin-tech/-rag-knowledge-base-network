package com.bin.ragknowledge.enums;

/**
 * 向量嵌入配置键常量
 * 用于统一管理Embedding配置的各项配置键
 */
public class EmbeddingConfigKey {

    /** 模式配置键 */
    public static final String MODE = "mode";

    /** DashScope API密钥 */
    public static final String DASHSCOPE_API_KEY = "dashscope_apiKey";

    /** DashScope模型名称 */
    public static final String DASHSCOPE_MODEL_NAME = "dashscope_modelName";

    /** Ollama服务地址 */
    public static final String OLLAMA_BASE_URL = "ollama_baseUrl";

    /** Ollama模型名称 */
    public static final String OLLAMA_MODEL_NAME = "ollama_modelName";

    /** Ollama超时时间 */
    public static final String OLLAMA_TIMEOUT = "ollama_timeout";

    /** OpenAI API密钥 */
    public static final String OPENAI_API_KEY = "openai_apiKey";

    /** OpenAI服务地址 */
    public static final String OPENAI_BASE_URL = "openai_baseUrl";

    /** OpenAI模型名称 */
    public static final String OPENAI_MODEL_NAME = "openai_modelName";

    /** OpenAI超时时间 */
    public static final String OPENAI_TIMEOUT = "openai_timeout";

    private EmbeddingConfigKey() {}
}