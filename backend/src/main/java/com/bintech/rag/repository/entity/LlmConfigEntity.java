package com.bintech.rag.repository.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * LLM/Embedding配置实体
 * 存储LLM和Embedding模型的配置信息，支持启用/停用功能
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("llm_config")
public class LlmConfigEntity extends BaseEntity {

    /**
     * 配置类型：LLM/EMBEDDING
     */
    private String configType;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 备注说明
     */
    private String remark;

    /**
     * 启用状态：true-启用，false-停用，同类型只能启用一个
     */
    private Boolean enabled;
}