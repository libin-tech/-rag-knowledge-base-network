package com.bin.ragknowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文本嵌入（Embedding）模型配置属性类
 * 用于读取和管理应用中 Embedding 模型相关的配置信息
 * Embedding 模型用于将文本转换为向量表示，是 RAG（检索增强生成）系统的核心组件
 * 支持两种模式：DashScope（阿里云通义千问）和 Ollama（本地部署）
 * 配置前缀为 "embedding"，对应 application.yml 中的 embedding 配置节
 */
@Data
@Component
@ConfigurationProperties(prefix = "embedding")
public class EmbeddingProperties {

    /**
     * Embedding 模型模式选择
     * 可选值：dashscope（阿里云通义千问）、ollama（本地部署）
     * 默认值为 dashscope
     */
    private String mode = "dashscope";

    /**
     * DashScope（阿里云通义千问）Embedding 配置
     * 当 mode 为 dashscope 时使用
     */
    private DashScope dashscope = new DashScope();

    /**
     * Ollama（本地部署）Embedding 配置
     * 当 mode 为 ollama 时使用
     */
    private Ollama ollama = new Ollama();

    /**
     * DashScope Embedding 配置内部类
     * 封装阿里云通义千问 Embedding 服务所需的配置参数
     */
    @Data
    public static class DashScope {
        /**
         * API 密钥
         * 用于访问阿里云 DashScope Embedding 服务的认证凭证
         */
        private String apiKey;

        /**
         * Embedding 模型名称
         * 指定使用的文本嵌入模型，默认为 text-embedding-v3
         * text-embedding-v3 是阿里云提供的高性能 Embedding 模型
         */
        private String modelName = "text-embedding-v3";
    }

    /**
     * Ollama Embedding 配置内部类
     * 封装本地部署的 Ollama Embedding 服务所需的配置参数
     */
    @Data
    public static class Ollama {
        /**
         * 服务基础 URL
         * Ollama 本地服务的地址，默认运行在 11434 端口
         */
        private String baseUrl = "http://localhost:11434";

        /**
         * Embedding 模型名称
         * 指定使用的本地嵌入模型，默认为 nomic-embed-text
         * nomic-embed-text 是一个轻量高效的开源 Embedding 模型
         */
        private String modelName = "nomic-embed-text";

        /**
         * 请求超时时间
         * 设置请求的最大等待时间，默认为 60 秒
         * 格式为带单位的字符串，如 "60s"、"5m" 等
         */
        private String timeout = "60s";
    }
}
