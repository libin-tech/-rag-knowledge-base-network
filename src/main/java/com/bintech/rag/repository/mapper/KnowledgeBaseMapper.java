package com.bintech.rag.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bintech.rag.repository.entity.KnowledgeBaseEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库 Mapper 接口
 */
@Mapper
public interface KnowledgeBaseMapper extends BaseMapper<KnowledgeBaseEntity> {
}