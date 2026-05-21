package com.bintech.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RAG（检索增强生成）配置属性类
 * 用于读取和管理 RAG 系统的核心配置参数
 * RAG 系统包含两个关键阶段：文档分块（Chunking）和检索（Retrieval）
 * 配置前缀为 "rag"，对应 application.yml 中的 rag 配置节
 */
@Data
@Component
@ConfigurationProperties(prefix = "rag")
public class RagProperties {

    /**
     * 文档分块配置
     * 控制如何将长文档切分成较小的文本块
     */
    private Chunk chunk = new Chunk();

    /**
     * 检索配置
     * 控制从向量数据库中检索相似结果的参数
     */
    private Retrieval retrieval = new Retrieval();

    /**
     * 文档分块配置内部类
     * 定义文本分割的参数，影响文档如何被切分为可处理的片段
     */
    @Data
    public static class Chunk {
        /**
         * 最大文本块大小
         * 每个文本段（Segment）的最大字符数，默认为 500 字符
         * 较大的块包含更多上下文但可能引入噪声，较小的块更精确但可能丢失上下文
         */
        private int maxSegmentSize = 500;

        /**
         * 文本块重叠大小
         * 相邻文本块之间的重叠字符数，默认为 50 字符
         * 重叠可以避免在切分点处丢失重要的语义信息
         */
        private int maxOverlapSize = 50;
    }

    /**
     * 检索配置内部类
     * 定义向量检索时的参数，控制返回结果的数量和质量阈值
     */
    @Data
    public static class Retrieval {
        /**
         * 最大返回结果数
         * 每次检索返回的最多结果数量，默认为 5 条
         * 较多的结果提供更多上下文但可能引入不相关信息
         */
        private int maxResults = 5;

        /**
         * 最小相似度得分
         * 结果被接受的最低相似度阈值，默认为 0.7
         * 只有相似度得分大于等于此值的结果才会被返回
         * 值越高结果越精确但可能减少返回数量
         */
        private double minScore = 0.7;
    }
}
