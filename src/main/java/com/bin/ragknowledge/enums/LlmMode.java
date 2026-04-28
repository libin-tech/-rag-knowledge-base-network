package com.bin.ragknowledge.enums;

/**
 * LLM模式枚举
 * 支持DashScope、Ollama、OpenAI三种LLM模型
 */
public class LlmMode {

    /** DashScope模式 */
    public static final String DASHSCOPE = "dashscope";

    /** Ollama模式 */
    public static final String OLLAMA = "ollama";

    /** OpenAI模式 */
    public static final String OPENAI = "openai";

    private LlmMode() {}
}