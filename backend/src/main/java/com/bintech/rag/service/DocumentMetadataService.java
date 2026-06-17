package com.bintech.rag.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bintech.rag.repository.dao.DocumentMetadataDAO;
import com.bintech.rag.repository.entity.DocumentMetadataEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentMetadataService {

    private final DocumentMetadataDAO documentMetadataDAO;

    public boolean save(DocumentMetadataEntity entity) {
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return documentMetadataDAO.insert(entity) > 0;
    }

    public boolean updateById(DocumentMetadataEntity entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return documentMetadataDAO.updateById(entity) > 0;
    }

    public boolean deleteById(String id) {
        return documentMetadataDAO.deleteById(id) > 0;
    }

    public DocumentMetadataEntity getById(String id) {
        return documentMetadataDAO.selectById(id);
    }

    public List<DocumentMetadataEntity> listAll() {
        return documentMetadataDAO.selectAll();
    }

    public List<DocumentMetadataEntity> listByKnowledgeBaseId(String knowledgeBaseId) {
        return documentMetadataDAO.selectByKnowledgeBaseId(knowledgeBaseId);
    }

    public Page<DocumentMetadataEntity> page(int current, int size) {
        return documentMetadataDAO.selectPage(current, size);
    }

    public Page<DocumentMetadataEntity> pageByKnowledgeBaseId(int current, int size, String knowledgeBaseId) {
        return documentMetadataDAO.selectPageByKnowledgeBaseId(current, size, knowledgeBaseId);
    }

    public long countByKnowledgeBaseId(String knowledgeBaseId) {
        return documentMetadataDAO.countByKnowledgeBaseId(knowledgeBaseId);
    }
}
