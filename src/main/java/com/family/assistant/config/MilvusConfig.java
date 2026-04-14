package com.family.assistant.config;

import com.alibaba.dashscope.utils.JsonUtils;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.client.MilvusServiceClient;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus 向量数据库配置类
 * 负责创建和配置 Milvus Embedding Store Bean，用于存储和检索向量数据
 * Milvus 是一个高性能的开源向量数据库，支持大规模的向量相似度检索
 * 在 RAG 系统中用于存储文档片段的 Embedding 向量，并提供基于语义相似度的检索能力
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MilvusConfig {

    /**
     * 注入 Milvus 配置属性
     * 通过构造函数注入，包含数据库连接信息和集合配置
     */
    private final MilvusProperties milvusProperties;

    /**
     * 注入嵌入模型
     * 用于在存储和检索时自动进行文本与向量之间的转换
     */
    private final EmbeddingModel embeddingModel;

    /**
     * 创建向量存储 Bean
     * 配置并初始化 Milvus Embedding Store，连接到 Milvus 数据库
     * 该 Store 用于存储和检索 TextSegment（文本片段）的向量表示
     *
     * 配置项包括：
     * - 连接信息：主机地址、端口、用户名和密码
     * - 集合配置：集合名称和向量维度
     * - 一致性级别：使用 STRONG 强一致性，确保读取到最新的数据
     *
     * @return EmbeddingStore&lt;TextSegment&gt; 实例，用于后续的向量存储和相似度检索操作
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {


        // 构建 Milvus 向量存储，配置数据库连接和集合参数
        return MilvusEmbeddingStore.builder()
                // 设置 Milvus 服务器主机地址
                .host(milvusProperties.getHost())
                // 设置 Milvus 服务器 gRPC 端口
                .port(milvusProperties.getPort())
                // 设置数据库认证用户名（如果启用了认证）
                .username(milvusProperties.getUsername())
                // 设置数据库认证密码（如果启用了认证）
                .password(milvusProperties.getPassword())
                // 设置向量集合名称，用于存储和检索文档片段
                .collectionName(milvusProperties.getCollectionName())
                // 设置向量维度，必须与 Embedding 模型输出的维度一致
                .dimension(milvusProperties.getDimension())
                // 设置一致性级别为 STRONG（强一致性）
                // 强一致性确保每次读取都能获取到最新写入的数据，适用于对数据实时性要求较高的场景
                .consistencyLevel(ConsistencyLevelEnum.STRONG)
                .build();
    }
}
