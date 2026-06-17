package com.bintech.rag.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bintech.rag.repository.entity.LlmConfigEntity;
import com.bintech.rag.repository.mapper.LlmConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
class LlmConfigDAOImpl implements LlmConfigDAO {

    private final LlmConfigMapper llmConfigMapper;

    @Override
    public int insert(LlmConfigEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("配置实体不能为空");
        }
        return llmConfigMapper.insert(entity);
    }

    @Override
    public int updateById(LlmConfigEntity entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("配置实体及ID不能为空");
        }
        return llmConfigMapper.updateById(entity);
    }

    @Override
    public int update(LlmConfigEntity entity, String configType, String configKey) {
        if (configType == null || configType.trim().isEmpty()) {
            throw new IllegalArgumentException("配置类型不能为空");
        }
        if (configKey == null) {
            throw new IllegalArgumentException("配置键不能为空");
        }
        return llmConfigMapper.update(entity, new LambdaUpdateWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getConfigKey, configKey));
    }

    @Override
    public int updateEnabledByConfigKey(String configType, String configKey, boolean enabled, String modifier) {
        if (configType == null || configType.trim().isEmpty()) {
            throw new IllegalArgumentException("配置类型不能为空");
        }
        if (configKey == null) {
            throw new IllegalArgumentException("配置键不能为空");
        }
        return llmConfigMapper.update(null, new LambdaUpdateWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getConfigKey, configKey)
                .set(LlmConfigEntity::getEnabled, enabled)
                .set(LlmConfigEntity::getModifier, modifier));
    }

    @Override
    public List<LlmConfigEntity> selectByType(String configType) {
        if (configType == null || configType.trim().isEmpty()) {
            throw new IllegalArgumentException("配置类型不能为空");
        }
        return llmConfigMapper.selectList(new LambdaQueryWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType));
    }

    @Override
    public LlmConfigEntity selectByTypeAndKey(String configType, String configKey) {
        if (configType == null || configType.trim().isEmpty()) {
            throw new IllegalArgumentException("配置类型不能为空");
        }
        if (configKey == null) {
            throw new IllegalArgumentException("配置键不能为空");
        }
        return llmConfigMapper.selectOne(new LambdaQueryWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getConfigKey, configKey));
    }

    @Override
    public List<LlmConfigEntity> selectEnabledByTypeAndKeyLike(String configType, String keyPattern) {
        if (configType == null || configType.trim().isEmpty()) {
            throw new IllegalArgumentException("配置类型不能为空");
        }
        if (keyPattern == null) {
            throw new IllegalArgumentException("配置键匹配模式不能为空");
        }
        return llmConfigMapper.selectList(new LambdaQueryWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getEnabled, true)
                .like(LlmConfigEntity::getConfigKey, keyPattern));
    }
}
