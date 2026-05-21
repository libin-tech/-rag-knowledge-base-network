package com.bintech.rag.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bintech.rag.repository.entity.DocumentMetadataEntity;
import com.bintech.rag.repository.mapper.DocumentMetadataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentMetadataService {

    private final DocumentMetadataMapper documentMetadataMapper;

    public boolean save(DocumentMetadataEntity entity) {
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return documentMetadataMapper.insert(entity) > 0;
    }

    public boolean updateById(DocumentMetadataEntity entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return documentMetadataMapper.updateById(entity) > 0;
    }

    public boolean deleteById(String id) {
        return documentMetadataMapper.deleteById(id) > 0;
    }

    public DocumentMetadataEntity getById(String id) {
        return documentMetadataMapper.selectById(id);
    }

    public List<DocumentMetadataEntity> listAll() {
        return documentMetadataMapper.selectList(null);
    }

    /**
     * 获取指定知识库的文档列表
     */
    public List<DocumentMetadataEntity> listByKnowledgeBaseId(String knowledgeBaseId) {
        QueryWrapper<DocumentMetadataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("knowledge_base_id", knowledgeBaseId);
        queryWrapper.orderByDesc("upload_time");
        return documentMetadataMapper.selectList(queryWrapper);
    }

    public Page<DocumentMetadataEntity> page(int current, int size) {
        Page<DocumentMetadataEntity> page = new Page<>(current, size);
        QueryWrapper<DocumentMetadataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("upload_time");
        return documentMetadataMapper.selectPage(page, queryWrapper);
    }

    /**
     * 分页查询指定知识库的文档
     */
    public Page<DocumentMetadataEntity> pageByKnowledgeBaseId(int current, int size, String knowledgeBaseId) {
        Page<DocumentMetadataEntity> page = new Page<>(current, size);
        QueryWrapper<DocumentMetadataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("knowledge_base_id", knowledgeBaseId);
        queryWrapper.orderByDesc("upload_time");
        return documentMetadataMapper.selectPage(page, queryWrapper);
    }

    /**
     * 统计指定知识库的文档数量
     */
    public long countByKnowledgeBaseId(String knowledgeBaseId) {
        QueryWrapper<DocumentMetadataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("knowledge_base_id", knowledgeBaseId);
        return documentMetadataMapper.selectCount(queryWrapper);
    }
}