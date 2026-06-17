package com.bintech.rag.repository.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bintech.rag.repository.entity.DocumentMetadataEntity;

import java.util.List;

public interface DocumentMetadataDAO {

    int insert(DocumentMetadataEntity entity);

    int updateById(DocumentMetadataEntity entity);

    int deleteById(String id);

    DocumentMetadataEntity selectById(String id);

    List<DocumentMetadataEntity> selectAll();

    List<DocumentMetadataEntity> selectByKnowledgeBaseId(String knowledgeBaseId);

    Page<DocumentMetadataEntity> selectPage(int current, int size);

    Page<DocumentMetadataEntity> selectPageByKnowledgeBaseId(int current, int size, String knowledgeBaseId);

    long countByKnowledgeBaseId(String knowledgeBaseId);

    List<DocumentMetadataEntity> selectByKnowledgeBaseIdWithDeletedCheck(String knowledgeBaseId);
}
