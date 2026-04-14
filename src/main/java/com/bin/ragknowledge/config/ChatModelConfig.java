package com.bin.ragknowledge.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.dashscope.QwenChatModel;
import dev.langchain4j.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 聊天模型配置类
 * 负责创建和配置 ChatLanguageModel Bean，用于处理对话和文本生成任务
 * 根据配置文件中的模式设置，动态选择使用 DashScope（阿里云通义千问）或 Ollama（本地部署）
 * 该类使用 LangChain4j 框架来集成不同的大语言模型服务
 */
@Configuration
@RequiredArgsConstructor
public class ChatModelConfig {

    /**
     * 注入 LLM 配置属性
     * 通过构造函数注入，包含模型模式选择及各模式的详细配置
     */
    private final LlmProperties llmProperties;

    /**
     * 创建聊天语言模型 Bean
     * 根据 llm.mode 配置项决定使用哪种模型实现：
     * - 当 mode 为 "ollama" 时，使用本地部署的 Ollama 服务
     * - 其他情况（默认），使用阿里云 DashScope 服务
     *
     * @return ChatLanguageModel 实例，用于后续的对话和文本生成操作
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        // 判断是否使用 Ollama 本地模式（忽略大小写比较）
        if ("ollama".equalsIgnoreCase(llmProperties.getMode())) {
            // 构建 Ollama 聊天模型，配置基础地址、模型名称和超时时间
            // Duration.parse 需要 ISO-8601 格式，因此添加 "PT" 前缀并将超时单位转为大写
            return OllamaChatModel.builder()
                    .baseUrl(llmProperties.getOllama().getBaseUrl())
                    .modelName(llmProperties.getOllama().getModelName())
                    .timeout(Duration.parse("PT" + llmProperties.getOllama().getTimeout().toUpperCase()))
                    .build();
        } else {
            // 构建阿里云 DashScope 聊天模型，使用通义千问服务
            // 配置 API 密钥、模型名称和服务端点地址
            return QwenChatModel.builder()
                    .apiKey(llmProperties.getDashscope().getApiKey())
                    .modelName(llmProperties.getDashscope().getModelName())
                    .build();
        }
    }

    /**
     * 创建流式聊天语言模型 Bean
     * 用于支持流式输出，实现打字机效果
     *
     * @return StreamingChatLanguageModel 实例，用于流式对话
     */
    @Bean
    public StreamingChatLanguageModel streamingChatLanguageModel() {
        // 判断是否使用 Ollama 本地模式（忽略大小写比较）
        if ("ollama".equalsIgnoreCase(llmProperties.getMode())) {
            // 构建 Ollama 流式聊天模型
            return OllamaStreamingChatModel.builder()
                    .baseUrl(llmProperties.getOllama().getBaseUrl())
                    .modelName(llmProperties.getOllama().getModelName())
                    .timeout(Duration.parse("PT" + llmProperties.getOllama().getTimeout().toUpperCase()))
                    .build();
        } else {
            // 构建阿里云 DashScope 流式聊天模型
            return QwenStreamingChatModel.builder()
                    .apiKey(llmProperties.getDashscope().getApiKey())
                    .modelName(llmProperties.getDashscope().getModelName())
                    .build();
        }
    }
}
