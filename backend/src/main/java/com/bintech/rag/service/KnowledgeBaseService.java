package com.bintech.rag.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bintech.rag.repository.dao.DocumentMetadataDAO;
import com.bintech.rag.repository.dao.KnowledgeBaseDAO;
import com.bintech.rag.repository.entity.DocumentMetadataEntity;
import com.bintech.rag.repository.entity.KnowledgeBaseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库服务
 * <p>
 * 负责知识库的增删改查操作，作为数据隔离的核心服务。
 * 不再自动初始化默认知识库，需通过系统页面手动创建。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeBaseDAO knowledgeBaseDAO;
    private final DocumentMetadataDAO documentMetadataDAO;
    private final MessageChannelService messageChannelService;
    private final RagService ragService;

    /**
     * 保存知识库（创建时同步初始化消息渠道）
     * 
     * @param entity 知识库实体
     * @return 创建的知识库实体
     * @throws IllegalArgumentException 参数校验失败时抛出
     * @throws RuntimeException 创建失败时抛出
     */
    @Transactional
    public KnowledgeBaseEntity save(KnowledgeBaseEntity entity) {
        // 参数校验
        if (entity == null) {
            throw new IllegalArgumentException("知识库实体不能为空");
        }
        
        if (entity.getName() == null || entity.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("知识库名称不能为空");
        }
        
        if (entity.getCreator() == null || entity.getCreator().trim().isEmpty()) {
            throw new IllegalArgumentException("创建人不能为空");
        }
        
        // 检查名称是否重复
        if (existsByName(entity.getName())) {
            throw new IllegalArgumentException("知识库名称已存在: " + entity.getName());
        }
        
        // 生成ID（如果未指定）
        if (entity.getId() == null || entity.getId().trim().isEmpty()) {
            entity.setId(cn.hutool.core.util.IdUtil.randomUUID());
        }
        
        // 设置默认值
        if (entity.getEnabled() == null) {
            entity.setEnabled(true);
        }
        
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        entity.setModifier(entity.getCreator());
        
        try {
            knowledgeBaseDAO.insert(entity);
            log.info("知识库记录已插入数据库: id={}, name={}", entity.getId(), entity.getName());
            
            // 初始化消息渠道
            messageChannelService.initChannelsForKnowledgeBase(entity.getId(), entity.getCreator());
            
            log.info("知识库 {} 创建成功，已初始化消息渠道", entity.getId());
            return entity;
        } catch (Exception e) {
            log.error("创建知识库失败: id={}, name={}, error={}", entity.getId(), entity.getName(), e.getMessage(), e);
            throw new RuntimeException("创建知识库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新知识库
     * 
     * @param entity 知识库实体
     * @return 更新是否成功
     * @throws IllegalArgumentException 参数校验失败时抛出
     * @throws RuntimeException 更新失败时抛出
     */
    public boolean updateById(KnowledgeBaseEntity entity) {
        // 参数校验
        if (entity == null) {
            throw new IllegalArgumentException("知识库实体不能为空");
        }
        
        if (entity.getId() == null || entity.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        
        // 检查知识库是否存在
        KnowledgeBaseEntity existing = knowledgeBaseDAO.selectById(entity.getId());
        if (existing == null) {
            throw new IllegalArgumentException("知识库不存在: " + entity.getId());
        }
        
        // 如果名称被修改，检查是否重复
        if (entity.getName() != null && !entity.getName().equals(existing.getName())) {
            if (existsByName(entity.getName())) {
                throw new IllegalArgumentException("知识库名称已存在: " + entity.getName());
            }
        }
        
        entity.setUpdateTime(LocalDateTime.now());
        
        try {
            int rows = knowledgeBaseDAO.updateById(entity);
            if (rows > 0) {
                log.info("知识库更新成功: id={}", entity.getId());
                return true;
            } else {
                log.warn("知识库更新失败，未找到匹配记录: id={}", entity.getId());
                return false;
            }
        } catch (Exception e) {
            log.error("更新知识库失败: id={}, error={}", entity.getId(), e.getMessage(), e);
            throw new RuntimeException("更新知识库失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除知识库（级联删除相关数据）
     */
    @Transactional
    public Map<String, Object> deleteById(String id, boolean deleteRelatedData) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查是否存在相关数据
        long documentCount = countDocuments(id);
        long channelCount = messageChannelService.countByKnowledgeBaseId(id);
        boolean hasVectorData = hasVectorData(id);
        
        if (!deleteRelatedData) {
            result.put("success", false);
            result.put("hasDocuments", documentCount > 0);
            result.put("hasChannels", channelCount > 0);
            result.put("hasVectorData", hasVectorData);
            result.put("message", "知识库下存在相关数据，无法直接删除");
            return result;
        }
        
        // 级联删除
        if (documentCount > 0) {
            deleteDocuments(id);
        }
        
        if (channelCount > 0) {
            messageChannelService.deleteByKnowledgeBaseId(id);
        }
        
        if (hasVectorData) {
            deleteVectorData(id);
        }
        
        // 删除知识库
        knowledgeBaseDAO.deleteById(id);
        
        result.put("success", true);
        result.put("message", "知识库及相关数据已删除");
        log.info("知识库 {} 及相关数据已删除", id);
        
        return result;
    }

    /**
     * 根据ID获取知识库
     */
    public KnowledgeBaseEntity getById(String id) {
        return knowledgeBaseDAO.selectById(id);
    }

    /**
     * 获取所有启用的知识库
     */
    public List<KnowledgeBaseEntity> listEnabled() {
        return knowledgeBaseDAO.selectEnabled();
    }

    /**
     * 获取所有知识库
     */
    public List<KnowledgeBaseEntity> listAll() {
        return knowledgeBaseDAO.selectAll();
    }

    /**
     * 分页查询知识库
     */
    public Page<KnowledgeBaseEntity> page(int current, int size) {
        return knowledgeBaseDAO.selectPage(current, size);
    }

    /**
     * 获取默认知识库
     */
    public KnowledgeBaseEntity getDefault() {
        return knowledgeBaseDAO.selectById("default");
    }

    /**
     * 检查知识库是否存在
     */
    public boolean exists(String id) {
        return knowledgeBaseDAO.selectById(id) != null;
    }

    /**
     * 检查知识库名称是否已存在
     */
    public boolean existsByName(String name) {
        return knowledgeBaseDAO.countByName(name) > 0;
    }

    /**
     * 统计知识库下的文档数量
     */
    public long countDocuments(String knowledgeBaseId) {
        return documentMetadataDAO.countByKnowledgeBaseId(knowledgeBaseId);
    }

    /**
     * 检查知识库是否存在向量数据
     */
    public boolean hasVectorData(String knowledgeBaseId) {
        KnowledgeBaseEntity kb = getById(knowledgeBaseId);
        if (kb == null) {
            return false;
        }
        // 如果知识库下有文档，则认为有向量数据
        return countDocuments(knowledgeBaseId) > 0;
    }

    /**
     * 删除知识库下的所有文档
     */
    @Transactional
    public void deleteDocuments(String knowledgeBaseId) {
        List<DocumentMetadataEntity> documents = documentMetadataDAO.selectByKnowledgeBaseIdWithDeletedCheck(knowledgeBaseId);
        
        for (DocumentMetadataEntity doc : documents) {
            // 删除向量数据
            if (doc.getVectorIds() != null && !doc.getVectorIds().isEmpty()) {
                List<String> vectorIds = cn.hutool.json.JSONUtil.toList(doc.getVectorIds(), String.class);
                ragService.deleteDocumentVectors(vectorIds);
            }
            documentMetadataDAO.deleteById(doc.getId());
        }
        log.info("知识库 {} 的文档已删除", knowledgeBaseId);
    }

    /**
     * 删除知识库的向量数据
     */
    public void deleteVectorData(String knowledgeBaseId) {
        // 通过删除文档来删除向量数据
        deleteDocuments(knowledgeBaseId);
    }
}