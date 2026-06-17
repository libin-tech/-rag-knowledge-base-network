package com.bintech.rag.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bintech.rag.repository.entity.DocumentMetadataEntity;
import com.bintech.rag.repository.mapper.DocumentMetadataMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
class DocumentMetadataDAOImpl implements DocumentMetadataDAO {

    private final DocumentMetadataMapper documentMetadataMapper;

    @Override
    public int insert(DocumentMetadataEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("文档元数据实体不能为空");
        }
        return documentMetadataMapper.insert(entity);
    }

    @Override
    public int updateById(DocumentMetadataEntity entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("文档元数据实体及ID不能为空");
        }
        return documentMetadataMapper.updateById(entity);
    }

    @Override
    public int deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("文档ID不能为空");
        }
        return documentMetadataMapper.deleteById(id);
    }

    @Override
    public DocumentMetadataEntity selectById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("文档ID不能为空");
        }
        return documentMetadataMapper.selectById(id);
    }

    @Override
    public List<DocumentMetadataEntity> selectAll() {
        return documentMetadataMapper.selectList(null);
    }

    @Override
    public List<DocumentMetadataEntity> selectByKnowledgeBaseId(String knowledgeBaseId) {
        if (knowledgeBaseId == null || knowledgeBaseId.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        return documentMetadataMapper.selectList(new QueryWrapper<DocumentMetadataEntity>()
                .eq("knowledge_base_id", knowledgeBaseId)
                .orderByDesc("upload_time"));
    }

    @Override
    public Page<DocumentMetadataEntity> selectPage(int current, int size) {
        if (current < 1 || size < 1) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        Page<DocumentMetadataEntity> page = new Page<>(current, size);
        return documentMetadataMapper.selectPage(page, new QueryWrapper<DocumentMetadataEntity>()
                .orderByDesc("upload_time"));
    }

    @Override
    public Page<DocumentMetadataEntity> selectPageByKnowledgeBaseId(int current, int size, String knowledgeBaseId) {
        if (current < 1 || size < 1) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        if (knowledgeBaseId == null || knowledgeBaseId.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        Page<DocumentMetadataEntity> page = new Page<>(current, size);
        return documentMetadataMapper.selectPage(page, new QueryWrapper<DocumentMetadataEntity>()
                .eq("knowledge_base_id", knowledgeBaseId)
                .orderByDesc("upload_time"));
    }

    @Override
    public long countByKnowledgeBaseId(String knowledgeBaseId) {
        if (knowledgeBaseId == null || knowledgeBaseId.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        return documentMetadataMapper.selectCount(new QueryWrapper<DocumentMetadataEntity>()
                .eq("knowledge_base_id", knowledgeBaseId));
    }

    @Override
    public List<DocumentMetadataEntity> selectByKnowledgeBaseIdWithDeletedCheck(String knowledgeBaseId) {
        if (knowledgeBaseId == null || knowledgeBaseId.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        return documentMetadataMapper.selectList(new QueryWrapper<DocumentMetadataEntity>()
                .eq("knowledge_base_id", knowledgeBaseId)
                .eq("deleted", false));
    }
}
