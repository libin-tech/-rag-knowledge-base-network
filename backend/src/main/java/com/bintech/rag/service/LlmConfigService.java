package com.bintech.rag.service;

import cn.hutool.core.util.IdUtil;
import com.bintech.rag.config.EmbeddingConfig;
import com.bintech.rag.config.LlmConfig;
import com.bintech.rag.enums.EmbeddingConfigKey;
import com.bintech.rag.enums.LlmConfigKey;
import com.bintech.rag.repository.dao.LlmConfigDAO;
import com.bintech.rag.repository.entity.LlmConfigEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmConfigService {

    private final LlmConfigDAO llmConfigDAO;

    public static final String CONFIG_TYPE_LLM = "LLM";
    public static final String CONFIG_TYPE_EMBEDDING = "EMBEDDING";

    public List<LlmConfigEntity> listByType(String configType) {
        return llmConfigDAO.selectByType(configType);
    }

    public Map<String, String> getConfigMap(String configType) {
        List<LlmConfigEntity> list = listByType(configType);
        return list.stream()
                .collect(Collectors.toMap(
                        LlmConfigEntity::getConfigKey,
                        LlmConfigEntity::getConfigValue,
                        (v1, v2) -> v1
                ));
    }

    public Map<String, String> getEmbeddingConfigMap(String configType) {
        List<LlmConfigEntity> list = listByType(configType);
        return list.stream()
                .collect(Collectors.toMap(
                        LlmConfigEntity::getConfigKey,
                        LlmConfigEntity::getConfigValue,
                        (v1, v2) -> v1
                ));
    }

    public LlmConfig getLlmConfig() {
        Map<String, String> map = getConfigMap(CONFIG_TYPE_LLM);
        LlmConfig config = new LlmConfig();

        if (map.containsKey(LlmConfigKey.MODE.getValue())) {
            config.setMode(map.get(LlmConfigKey.MODE.getValue()));
        }
        config.setDashscopeApiKey(map.get(LlmConfigKey.DASHSCOPE_API_KEY.getValue()));
        if (map.containsKey(LlmConfigKey.DASHSCOPE_MODEL_NAME.getValue())) {
            config.setDashscopeModelName(map.get(LlmConfigKey.DASHSCOPE_MODEL_NAME.getValue()));
        }
        if (map.containsKey(LlmConfigKey.OLLAMA_BASE_URL.getValue())) {
            config.setOllamaBaseUrl(map.get(LlmConfigKey.OLLAMA_BASE_URL.getValue()));
        }
        if (map.containsKey(LlmConfigKey.OLLAMA_MODEL_NAME.getValue())) {
            config.setOllamaModelName(map.get(LlmConfigKey.OLLAMA_MODEL_NAME.getValue()));
        }
        if (map.containsKey(LlmConfigKey.OLLAMA_TIMEOUT.getValue())) {
            config.setOllamaTimeout(map.get(LlmConfigKey.OLLAMA_TIMEOUT.getValue()));
        }
        config.setOpenaiApiKey(map.get(LlmConfigKey.OPENAI_API_KEY.getValue()));
        if (map.containsKey(LlmConfigKey.OPENAI_BASE_URL.getValue())) {
            config.setOpenaiBaseUrl(map.get(LlmConfigKey.OPENAI_BASE_URL.getValue()));
        }
        if (map.containsKey(LlmConfigKey.OPENAI_MODEL_NAME.getValue())) {
            config.setOpenaiModelName(map.get(LlmConfigKey.OPENAI_MODEL_NAME.getValue()));
        }
        if (map.containsKey(LlmConfigKey.OPENAI_TIMEOUT.getValue())) {
            config.setOpenaiTimeout(map.get(LlmConfigKey.OPENAI_TIMEOUT.getValue()));
        }

        return config;
    }

    public EmbeddingConfig getEmbeddingConfig() {
        Map<String, String> map = getEmbeddingConfigMap(CONFIG_TYPE_EMBEDDING);
        EmbeddingConfig config = new EmbeddingConfig();

        if (map.containsKey(EmbeddingConfigKey.MODE.getValue())) {
            config.setMode(map.get(EmbeddingConfigKey.MODE.getValue()));
        }
        config.setDashscopeApiKey(map.get(EmbeddingConfigKey.DASHSCOPE_API_KEY.getValue()));
        if (map.containsKey(EmbeddingConfigKey.DASHSCOPE_MODEL_NAME.getValue())) {
            config.setDashscopeModelName(map.get(EmbeddingConfigKey.DASHSCOPE_MODEL_NAME.getValue()));
        }
        if (map.containsKey(EmbeddingConfigKey.OLLAMA_BASE_URL.getValue())) {
            config.setOllamaBaseUrl(map.get(EmbeddingConfigKey.OLLAMA_BASE_URL.getValue()));
        }
        if (map.containsKey(EmbeddingConfigKey.OLLAMA_MODEL_NAME.getValue())) {
            config.setOllamaModelName(map.get(EmbeddingConfigKey.OLLAMA_MODEL_NAME.getValue()));
        }
        if (map.containsKey(EmbeddingConfigKey.OLLAMA_TIMEOUT.getValue())) {
            config.setOllamaTimeout(map.get(EmbeddingConfigKey.OLLAMA_TIMEOUT.getValue()));
        }
        config.setOpenaiApiKey(map.get(EmbeddingConfigKey.OPENAI_API_KEY.getValue()));
        if (map.containsKey(EmbeddingConfigKey.OPENAI_BASE_URL.getValue())) {
            config.setOpenaiBaseUrl(map.get(EmbeddingConfigKey.OPENAI_BASE_URL.getValue()));
        }
        if (map.containsKey(EmbeddingConfigKey.OPENAI_MODEL_NAME.getValue())) {
            config.setOpenaiModelName(map.get(EmbeddingConfigKey.OPENAI_MODEL_NAME.getValue()));
        }
        if (map.containsKey(EmbeddingConfigKey.OPENAI_TIMEOUT.getValue())) {
            config.setOpenaiTimeout(map.get(EmbeddingConfigKey.OPENAI_TIMEOUT.getValue()));
        }

        return config;
    }

    public String getValue(String configType, String configKey) {
        LlmConfigEntity entity = llmConfigDAO.selectByTypeAndKey(configType, configKey);
        return entity != null ? entity.getConfigValue() : null;
    }

    @Transactional
    public void updateConfig(String configType, String configKey, String configValue, String modifier) {
        if (configType == null || configType.trim().isEmpty()) {
            throw new IllegalArgumentException("配置类型不能为空");
        }

        if (configKey == null) {
            throw new IllegalArgumentException("配置键不能为空");
        }

        if (modifier == null || modifier.trim().isEmpty()) {
            throw new IllegalArgumentException("修改人不能为空");
        }

        try {
            LlmConfigEntity entity = new LlmConfigEntity();
            entity.setConfigValue(configValue);
            entity.setModifier(modifier);
            int rows = llmConfigDAO.update(entity, configType, configKey);
            if (rows > 0) {
                log.info("配置更新成功: {}.{} = {}, modifier={}", configType, configKey, configValue, modifier);
            } else {
                LlmConfigEntity newEntity = new LlmConfigEntity();
                newEntity.setId(cn.hutool.core.util.IdUtil.randomUUID());
                newEntity.setConfigType(configType);
                newEntity.setConfigKey(configKey);
                newEntity.setConfigValue(configValue);
                newEntity.setCreator(modifier);
                newEntity.setModifier(modifier);
                newEntity.setEnabled(true);
                newEntity.setCreateTime(LocalDateTime.now());
                newEntity.setUpdateTime(LocalDateTime.now());
                llmConfigDAO.insert(newEntity);
                log.info("配置创建成功: {}.{} = {}, creator={}", configType, configKey, configValue, modifier);
            }
        } catch (Exception e) {
            log.error("配置更新/创建失败: configType={}, configKey={}, modifier={}, error={}",
                    configType, configKey, modifier, e.getMessage(), e);
            throw new RuntimeException("配置操作失败: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void saveOrUpdate(String configType, String configKey, String configValue, String remark, String creator) {
        LlmConfigEntity existing = llmConfigDAO.selectByTypeAndKey(configType, configKey);

        if (existing != null) {
            existing.setConfigValue(configValue);
            existing.setRemark(remark);
            llmConfigDAO.updateById(existing);
        } else {
            LlmConfigEntity entity = new LlmConfigEntity();
            entity.setId(IdUtil.randomUUID());
            entity.setConfigType(configType);
            entity.setConfigKey(configKey);
            entity.setConfigValue(configValue);
            entity.setRemark(remark);
            entity.setCreator(creator);
            entity.setModifier(creator);
            llmConfigDAO.insert(entity);
        }
    }

    @Transactional
    public void enableConfig(String configType, String configKey, String modifier) {
        String modelName = extractModelName(configKey);
        if (modelName != null) {
            disableAllByModelName(configType, modelName, modifier);
        }
        llmConfigDAO.updateEnabledByConfigKey(configType, configKey, true, modifier);
        log.info("配置启用成功: {}.{}", configType, configKey);
    }

    @Transactional
    public void disableConfig(String configType, String configKey, String modifier) {
        llmConfigDAO.updateEnabledByConfigKey(configType, configKey, false, modifier);
        log.info("配置停用成功: {}.{}", configType, configKey);
    }

    private void disableAllByModelName(String configType, String modelName, String modifier) {
        List<LlmConfigEntity> enabledConfigs = llmConfigDAO.selectEnabledByTypeAndKeyLike(configType, modelName);
        for (LlmConfigEntity config : enabledConfigs) {
            config.setEnabled(false);
            config.setModifier(modifier);
            llmConfigDAO.updateById(config);
            log.info("配置自动停用: {}.{}", configType, config.getConfigKey());
        }
    }

    private String extractModelName(String configKey) {
        if (configKey == null) {
            return null;
        }
        int underscoreIdx = configKey.lastIndexOf('_');
        return underscoreIdx > 0 ? configKey.substring(0, underscoreIdx) : null;
    }
}
