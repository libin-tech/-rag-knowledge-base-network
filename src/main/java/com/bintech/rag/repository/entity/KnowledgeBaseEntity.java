package com.bintech.rag.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识库实体类
 * <p>
 * 存储知识库的基本信息，作为文档和消息渠道的分组管理单元。
 * 所有数据以知识库为基准，提问时在对应的知识库中检索。
 * </p>
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("knowledge_base")
public class KnowledgeBaseEntity extends BaseEntity {

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean enabled;
}