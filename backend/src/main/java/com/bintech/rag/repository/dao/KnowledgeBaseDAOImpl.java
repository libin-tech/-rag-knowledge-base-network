package com.bintech.rag.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bintech.rag.repository.entity.KnowledgeBaseEntity;
import com.bintech.rag.repository.mapper.KnowledgeBaseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
class KnowledgeBaseDAOImpl implements KnowledgeBaseDAO {

    private final KnowledgeBaseMapper knowledgeBaseMapper;

    @Override
    public int insert(KnowledgeBaseEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("知识库实体不能为空");
        }
        return knowledgeBaseMapper.insert(entity);
    }

    @Override
    public int updateById(KnowledgeBaseEntity entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("知识库实体及ID不能为空");
        }
        return knowledgeBaseMapper.updateById(entity);
    }

    @Override
    public int deleteById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        return knowledgeBaseMapper.deleteById(id);
    }

    @Override
    public KnowledgeBaseEntity selectById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        return knowledgeBaseMapper.selectById(id);
    }

    @Override
    public List<KnowledgeBaseEntity> selectAll() {
        return knowledgeBaseMapper.selectList(null);
    }

    @Override
    public List<KnowledgeBaseEntity> selectEnabled() {
        return knowledgeBaseMapper.selectList(new QueryWrapper<KnowledgeBaseEntity>()
                .eq("enabled", true)
                .orderByDesc("create_time"));
    }

    @Override
    public Page<KnowledgeBaseEntity> selectPage(int current, int size) {
        if (current < 1 || size < 1) {
            throw new IllegalArgumentException("分页参数不合法");
        }
        Page<KnowledgeBaseEntity> page = new Page<>(current, size);
        return knowledgeBaseMapper.selectPage(page, new QueryWrapper<KnowledgeBaseEntity>()
                .orderByDesc("create_time"));
    }

    @Override
    public long countByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库名称不能为空");
        }
        return knowledgeBaseMapper.selectCount(new QueryWrapper<KnowledgeBaseEntity>()
                .eq("name", name)
                .eq("deleted", false));
    }
}
