package com.bintech.rag.repository.dao;

import com.bintech.rag.repository.entity.LlmConfigEntity;

import java.util.List;

public interface LlmConfigDAO {

    int insert(LlmConfigEntity entity);

    int updateById(LlmConfigEntity entity);

    int update(LlmConfigEntity entity, String configType, String configKey);

    int updateEnabledByConfigKey(String configType, String configKey, boolean enabled, String modifier);

    List<LlmConfigEntity> selectByType(String configType);

    LlmConfigEntity selectByTypeAndKey(String configType, String configKey);

    List<LlmConfigEntity> selectEnabledByTypeAndKeyLike(String configType, String keyPattern);
}
