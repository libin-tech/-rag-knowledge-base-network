package com.bin.ragknowledge.enums;

/**
 * Embedding模式枚举
 * 支持DashScope、Ollama、OpenAI三种Embedding模型
 */
public class EmbeddingMode {

    /** DashScope模式 */
    public static final String DASHSCOPE = "dashscope";

    /** Ollama模式 */
    public static final String OLLAMA = "ollama";

    /** OpenAI模式 */
    public static final String OPENAI = "openai";

    private EmbeddingMode() {}
}