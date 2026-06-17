package com.bintech.rag.repository.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bintech.rag.repository.entity.KnowledgeBaseEntity;

import java.util.List;

public interface KnowledgeBaseDAO {

    int insert(KnowledgeBaseEntity entity);

    int updateById(KnowledgeBaseEntity entity);

    int deleteById(String id);

    KnowledgeBaseEntity selectById(String id);

    List<KnowledgeBaseEntity> selectAll();

    List<KnowledgeBaseEntity> selectEnabled();

    Page<KnowledgeBaseEntity> selectPage(int current, int size);

    long countByName(String name);
}
