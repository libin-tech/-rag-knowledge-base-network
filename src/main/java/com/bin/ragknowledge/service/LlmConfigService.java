package com.bin.ragknowledge.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bin.ragknowledge.config.EmbeddingConfig;
import com.bin.ragknowledge.config.LlmConfig;
import com.bin.ragknowledge.enums.EmbeddingConfigKey;
import com.bin.ragknowledge.enums.LlmConfigKey;
import com.bin.ragknowledge.repository.entity.LlmConfigEntity;
import com.bin.ragknowledge.repository.mapper.LlmConfigMapper;

import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LlmConfigService {

    private final LlmConfigMapper llmConfigMapper;

    public static final String CONFIG_TYPE_LLM = "LLM";
    public static final String CONFIG_TYPE_EMBEDDING = "EMBEDDING";

    public List<LlmConfigEntity> listByType(String configType) {
        return llmConfigMapper.selectList(new LambdaQueryWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType));
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
        LambdaQueryWrapper<LlmConfigEntity> wrapper = new LambdaQueryWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getConfigKey, configKey);
        LlmConfigEntity entity = llmConfigMapper.selectOne(wrapper);
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
            LambdaUpdateWrapper<LlmConfigEntity> wrapper = new LambdaUpdateWrapper<LlmConfigEntity>()
                    .eq(LlmConfigEntity::getConfigType, configType)
                    .eq(LlmConfigEntity::getConfigKey, configKey);
            LlmConfigEntity entity = new LlmConfigEntity();
            entity.setConfigValue(configValue);
            entity.setModifier(modifier);
            int rows = llmConfigMapper.update(entity, wrapper);
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
                llmConfigMapper.insert(newEntity);
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
        LambdaQueryWrapper<LlmConfigEntity> wrapper = new LambdaQueryWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getConfigKey, configKey);
        LlmConfigEntity existing = llmConfigMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setConfigValue(configValue);
            existing.setRemark(remark);
            llmConfigMapper.updateById(existing);
        } else {
            LlmConfigEntity entity = new LlmConfigEntity();
            entity.setId(IdUtil.randomUUID());
            entity.setConfigType(configType);
            entity.setConfigKey(configKey);
            entity.setConfigValue(configValue);
            entity.setRemark(remark);
            entity.setCreator(creator);
            entity.setModifier(creator);
            llmConfigMapper.insert(entity);
        }
    }

    @Transactional
    public void enableConfig(String configType, String configKey, String modifier) {
        String modelName = extractModelName(configKey);
        if (modelName != null) {
            disableAllByModelName(configType, modelName, modifier);
        }
        LambdaUpdateWrapper<LlmConfigEntity> wrapper = new LambdaUpdateWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getConfigKey, configKey)
                .set(LlmConfigEntity::getEnabled, true)
                .set(LlmConfigEntity::getModifier, modifier);
        llmConfigMapper.update(null, wrapper);
        log.info("配置启用成功: {}.{}", configType, configKey);
    }

    @Transactional
    public void disableConfig(String configType, String configKey, String modifier) {
        LambdaUpdateWrapper<LlmConfigEntity> wrapper = new LambdaUpdateWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getConfigKey, configKey)
                .set(LlmConfigEntity::getEnabled, false)
                .set(LlmConfigEntity::getModifier, modifier);
        llmConfigMapper.update(null, wrapper);
        log.info("配置停用成功: {}.{}", configType, configKey);
    }

    private void disableAllByModelName(String configType, String modelName, String modifier) {
        List<LlmConfigEntity> enabledConfigs = llmConfigMapper.selectList(
                new LambdaQueryWrapper<LlmConfigEntity>()
                        .eq(LlmConfigEntity::getConfigType, configType)
                        .eq(LlmConfigEntity::getEnabled, true)
                        .like(LlmConfigEntity::getConfigKey, modelName)
        );
        for (LlmConfigEntity config : enabledConfigs) {
            config.setEnabled(false);
            config.setModifier(modifier);
            llmConfigMapper.updateById(config);
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
