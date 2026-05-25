package com.bintech.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Milvus 向量数据库配置属性类
 * 用于读取和管理应用中 Milvus 数据库的连接和集合配置信息
 * Milvus 是一个开源的向量数据库，用于存储和检索向量数据
 * 在 RAG 系统中用于存储文档的 Embedding 向量并执行相似度检索
 * 配置前缀为 "milvus"，对应 application.yml 中的 milvus 配置节
 */
@Data
@Component
@ConfigurationProperties(prefix = "milvus")
public class MilvusProperties {

    /**
     * Milvus 服务器主机地址
     * 指定 Milvus 服务的网络地址，默认为 localhost（本地部署）
     * 如果 Milvus 部署在远程服务器，需修改此值为对应的 IP 或域名
     */
    private String host = "localhost";

    /**
     * Milvus 服务器端口
     * Milvus gRPC 服务的监听端口，默认为 19530
     * 这是 Milvus 的默认 gRPC 端口，用于客户端与服务端通信
     */
    private int port = 19530;

    /**
     * Milvus 数据库用户名
     * 用于数据库认证，如果启用了认证功能则需要提供
     * 如果未启用认证可留空
     */
    private String username;

    /**
     * Milvus 数据库密码
     * 与用户名配套使用的认证密码
     * 如果未启用认证可留空
     */
    private String password;

    /**
     * 集合名称
     * 指定存储向量数据的集合（Collection）名称
     * 在此应用中用于存储家规文档的 Embedding 向量
     * 默认为 "Enterprise_Knowledge_Base"
     */
    private String collectionName = "Enterprise_Knowledge_Base";

    /**
     * 向量维度
     * 指定存储的向量数据的维度
     * 必须与 Embedding 模型输出的向量维度一致
     * 默认为 1024 维
     */
    private int dimension = 1024;
}
