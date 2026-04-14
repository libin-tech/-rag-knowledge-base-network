package com.bin.ragknowledge.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 大语言模型（LLM）配置属性类
 * 用于读取和管理应用中 LLM 相关的配置信息
 * 支持两种模式：DashScope（阿里云通义千问）和 Ollama（本地部署）
 * 配置前缀为 "llm"，对应 application.yml 中的 llm 配置节
 */
@Data
@Component
@ConfigurationProperties(prefix = "llm")
public class LlmProperties {

    /**
     * LLM 模式选择
     * 可选值：dashscope（阿里云通义千问）、ollama（本地部署）
     * 默认值为 dashscope
     */
    private String mode = "dashscope";

    /**
     * DashScope（阿里云通义千问）配置
     * 当 mode 为 dashscope 时使用
     */
    private DashScope dashscope = new DashScope();

    /**
     * Ollama（本地部署）配置
     * 当 mode 为 ollama 时使用
     */
    private Ollama ollama = new Ollama();

    /**
     * DashScope 配置内部类
     * 封装阿里云通义千问服务所需的配置参数
     */
    @Data
    public static class DashScope {
        /**
         * API 密钥
         * 用于访问阿里云 DashScope 服务的认证凭证
         */
        private String apiKey;

        /**
         * 模型名称
         * 指定使用的通义千问模型，默认为 qwen-plus
         * qwen-plus 是平衡性能和成本的通用模型
         */
        private String modelName = "qwen-plus";

    }

    /**
     * Ollama 配置内部类
     * 封装本地部署的 Ollama 服务所需的配置参数
     */
    @Data
    public static class Ollama {
        /**
         * 服务基础 URL
         * Ollama 本地服务的地址，默认运行在 11434 端口
         */
        private String baseUrl = "http://localhost:11434";

        /**
         * 模型名称
         * 指定使用的本地模型，默认为 qwen3
         */
        private String modelName = "qwen3";

        /**
         * 请求超时时间
         * 设置请求的最大等待时间，默认为 60 秒
         * 格式为带单位的字符串，如 "60s"、"5m" 等
         */
        private String timeout = "60s";
    }
}
