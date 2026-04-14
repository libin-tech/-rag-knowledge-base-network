package com.family.assistant.config;

import dev.langchain4j.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Embedding（文本嵌入）模型配置类
 * 负责创建和配置 EmbeddingModel Bean，用于将文本转换为向量表示
 * Embedding 模型是 RAG（检索增强生成）系统的核心组件，将文本语义映射到高维向量空间
 * 以便进行相似度计算和向量检索
 * 根据配置文件中的模式设置，动态选择使用 DashScope 或 Ollama 实现
 */
@Configuration
@RequiredArgsConstructor
public class EmbeddingModelConfig {

    /**
     * 注入 Embedding 配置属性
     * 通过构造函数注入，包含模型模式选择及各模式的详细配置
     */
    private final EmbeddingProperties embeddingProperties;

    /**
     * 创建嵌入模型 Bean
     * 根据 embedding.mode 配置项决定使用哪种模型实现：
     * - 当 mode 为 "ollama" 时，使用本地部署的 Ollama 服务
     * - 当 mode 为 "dashscope" 时，使用阿里云 DashScope 服务
     * - 其他情况抛出运行时异常，提示模型未配置
     *
     * @return EmbeddingModel 实例，用于后续的文本向量化操作
     * @throws RuntimeException 当配置的模式无法识别时抛出异常
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        // 判断是否使用 Ollama 本地模式（忽略大小写比较）
        if ("ollama".equalsIgnoreCase(embeddingProperties.getMode())) {
            // 构建 Ollama 嵌入模型，配置基础地址、模型名称和超时时间
            // Duration.parse 需要 ISO-8601 格式，因此添加 "PT" 前缀并将超时单位转为大写
            return OllamaEmbeddingModel.builder()
                    .baseUrl(embeddingProperties.getOllama().getBaseUrl())
                    .modelName(embeddingProperties.getOllama().getModelName())
                    .timeout(Duration.parse("PT" + embeddingProperties.getOllama().getTimeout().toUpperCase()))
                    .build();
        }
        // 判断是否使用阿里云 DashScope 模式（忽略大小写比较）
        if ("dashscope".equalsIgnoreCase(embeddingProperties.getMode())) {
            // 构建阿里云 DashScope 嵌入模型，使用通义千问 Embedding 服务
            // 配置 API 密钥和模型名称
            return QwenEmbeddingModel.builder()
                    .apiKey(embeddingProperties.getDashscope().getApiKey())
                    .modelName(embeddingProperties.getDashscope().getModelName())
                    .build();
        }

        // 无法识别的配置模式，抛出异常提示用户检查配置
        throw new RuntimeException("MODEL NOT SET");
    }
}
