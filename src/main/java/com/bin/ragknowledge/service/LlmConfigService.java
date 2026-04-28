package com.bin.ragknowledge.service;

import cn.hutool.core.util.IdUtil;
import com.bin.ragknowledge.config.EmbeddingConfig;
import com.bin.ragknowledge.config.LlmConfig;
import com.bin.ragknowledge.enums.EmbeddingConfigKey;
import com.bin.ragknowledge.enums.LlmConfigKey;
import com.bin.ragknowledge.repository.entity.LlmConfigEntity;
import com.bin.ragknowledge.repository.mapper.LlmConfigMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * LLM配置服务
 * 提供LLM和Embedding配置的查询、更新、保存、启用/停用等操作
 * 配置值从数据库读取，默认值在各配置类中定义
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmConfigService {

    private final LlmConfigMapper llmConfigMapper;

    /** 配置类型：LLM */
    public static final String CONFIG_TYPE_LLM = "LLM";
    /** 配置类型：EMBEDDING */
    public static final String CONFIG_TYPE_EMBEDDING = "EMBEDDING";

    /**
     * 根据配置类型查询配置列表
     *
     * @param configType 配置类型
     * @return 配置实体列表
     */
    public List<LlmConfigEntity> listByType(String configType) {
        return llmConfigMapper.selectList(new LambdaQueryWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getDeleted, false));
    }

    /**
     * 获取配置Map
     *
     * @param configType 配置类型
     * @return 配置键值对
     */
    public Map<String, String> getConfigMap(String configType) {
        List<LlmConfigEntity> list = listByType(configType);
        return list.stream()
                .collect(Collectors.toMap(
                        LlmConfigEntity::getConfigKey,
                        LlmConfigEntity::getConfigValue,
                        (v1, v2) -> v1
                ));
    }

    /**
     * 获取LLM配置
     * 从数据库读取配置值，未设置的项使用配置类中的默认值
     *
     * @return LLM配置对象
     */
    public LlmConfig getLlmConfig() {
        Map<String, String> map = getConfigMap(CONFIG_TYPE_LLM);
        LlmConfig config = new LlmConfig();

        if (map.containsKey(LlmConfigKey.MODE)) {
            config.setMode(map.get(LlmConfigKey.MODE));
        }
        config.setDashscopeApiKey(map.get(LlmConfigKey.DASHSCOPE_API_KEY));
        if (map.containsKey(LlmConfigKey.DASHSCOPE_MODEL_NAME)) {
            config.setDashscopeModelName(map.get(LlmConfigKey.DASHSCOPE_MODEL_NAME));
        }
        if (map.containsKey(LlmConfigKey.OLLAMA_BASE_URL)) {
            config.setOllamaBaseUrl(map.get(LlmConfigKey.OLLAMA_BASE_URL));
        }
        if (map.containsKey(LlmConfigKey.OLLAMA_MODEL_NAME)) {
            config.setOllamaModelName(map.get(LlmConfigKey.OLLAMA_MODEL_NAME));
        }
        if (map.containsKey(LlmConfigKey.OLLAMA_TIMEOUT)) {
            config.setOllamaTimeout(map.get(LlmConfigKey.OLLAMA_TIMEOUT));
        }
        config.setOpenaiApiKey(map.get(LlmConfigKey.OPENAI_API_KEY));
        if (map.containsKey(LlmConfigKey.OPENAI_BASE_URL)) {
            config.setOpenaiBaseUrl(map.get(LlmConfigKey.OPENAI_BASE_URL));
        }
        if (map.containsKey(LlmConfigKey.OPENAI_MODEL_NAME)) {
            config.setOpenaiModelName(map.get(LlmConfigKey.OPENAI_MODEL_NAME));
        }
        if (map.containsKey(LlmConfigKey.OPENAI_TIMEOUT)) {
            config.setOpenaiTimeout(map.get(LlmConfigKey.OPENAI_TIMEOUT));
        }

        return config;
    }

    /**
     * 获取Embedding配置
     * 从数据库读取配置值，未设置的项使用配置类中的默认值
     *
     * @return Embedding配置对象
     */
    public EmbeddingConfig getEmbeddingConfig() {
        Map<String, String> map = getConfigMap(CONFIG_TYPE_EMBEDDING);
        EmbeddingConfig config = new EmbeddingConfig();

        if (map.containsKey(EmbeddingConfigKey.MODE)) {
            config.setMode(map.get(EmbeddingConfigKey.MODE));
        }
        config.setDashscopeApiKey(map.get(EmbeddingConfigKey.DASHSCOPE_API_KEY));
        if (map.containsKey(EmbeddingConfigKey.DASHSCOPE_MODEL_NAME)) {
            config.setDashscopeModelName(map.get(EmbeddingConfigKey.DASHSCOPE_MODEL_NAME));
        }
        if (map.containsKey(EmbeddingConfigKey.OLLAMA_BASE_URL)) {
            config.setOllamaBaseUrl(map.get(EmbeddingConfigKey.OLLAMA_BASE_URL));
        }
        if (map.containsKey(EmbeddingConfigKey.OLLAMA_MODEL_NAME)) {
            config.setOllamaModelName(map.get(EmbeddingConfigKey.OLLAMA_MODEL_NAME));
        }
        if (map.containsKey(EmbeddingConfigKey.OLLAMA_TIMEOUT)) {
            config.setOllamaTimeout(map.get(EmbeddingConfigKey.OLLAMA_TIMEOUT));
        }
        config.setOpenaiApiKey(map.get(EmbeddingConfigKey.OPENAI_API_KEY));
        if (map.containsKey(EmbeddingConfigKey.OPENAI_BASE_URL)) {
            config.setOpenaiBaseUrl(map.get(EmbeddingConfigKey.OPENAI_BASE_URL));
        }
        if (map.containsKey(EmbeddingConfigKey.OPENAI_MODEL_NAME)) {
            config.setOpenaiModelName(map.get(EmbeddingConfigKey.OPENAI_MODEL_NAME));
        }
        if (map.containsKey(EmbeddingConfigKey.OPENAI_TIMEOUT)) {
            config.setOpenaiTimeout(map.get(EmbeddingConfigKey.OPENAI_TIMEOUT));
        }

        return config;
    }

    /**
     * 获取单个配置项的值
     *
     * @param configType 配置类型
     * @param configKey 配置键
     * @return 配置值，不存在则返回null
     */
    public String getValue(String configType, String configKey) {
        LambdaQueryWrapper<LlmConfigEntity> wrapper = new LambdaQueryWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getConfigKey, configKey)
                .eq(LlmConfigEntity::getDeleted, false);
        LlmConfigEntity entity = llmConfigMapper.selectOne(wrapper);
        return entity != null ? entity.getConfigValue() : null;
    }

    /**
     * 更新配置项
     *
     * @param configType 配置类型
     * @param configKey 配置键
     * @param configValue 配置值
     * @param modifier 修改人
     */
    @Transactional
    public void updateConfig(String configType, String configKey, String configValue, String modifier) {
        LambdaUpdateWrapper<LlmConfigEntity> wrapper = new LambdaUpdateWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getConfigKey, configKey)
                .eq(LlmConfigEntity::getDeleted, false);
        LlmConfigEntity entity = new LlmConfigEntity();
        entity.setConfigValue(configValue);
        entity.setModifier(modifier);
        int rows = llmConfigMapper.update(entity, wrapper);
        if (rows > 0) {
            log.info("配置更新成功: {}.{} = {}", configType, configKey, configValue);
        } else {
            log.warn("配置更新失败: {}.{}", configType, configKey);
        }
    }

    /**
     * 保存或更新配置项
     * 如果配置键已存在则更新，否则新增
     *
     * @param configType 配置类型
     * @param configKey 配置键
     * @param configValue 配置值
     * @param remark 备注
     * @param creator 创建人/修改人
     */
    @Transactional
    public void saveOrUpdate(String configType, String configKey, String configValue, String remark, String creator) {
        LambdaQueryWrapper<LlmConfigEntity> wrapper = new LambdaQueryWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getConfigKey, configKey)
                .eq(LlmConfigEntity::getDeleted, false);
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

    /**
     * 启用指定配置项
     * 启用后会自动停用同类型下其他相同的模型配置
     *
     * @param configType 配置类型
     * @param configKey 配置键
     * @param modifier 修改人
     */
    @Transactional
    public void enableConfig(String configType, String configKey, String modifier) {
        String modelName = extractModelName(configKey);
        if (modelName != null) {
            disableAllByModelName(configType, modelName, modifier);
        }
        LambdaUpdateWrapper<LlmConfigEntity> wrapper = new LambdaUpdateWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getConfigKey, configKey)
                .eq(LlmConfigEntity::getDeleted, false)
                .set(LlmConfigEntity::getEnabled, true)
                .set(LlmConfigEntity::getModifier, modifier);
        llmConfigMapper.update(null, wrapper);
        log.info("配置启用成功: {}.{}", configType, configKey);
    }

    /**
     * 停用指定配置项
     *
     * @param configType 配置类型
     * @param configKey 配置键
     * @param modifier 修改人
     */
    @Transactional
    public void disableConfig(String configType, String configKey, String modifier) {
        LambdaUpdateWrapper<LlmConfigEntity> wrapper = new LambdaUpdateWrapper<LlmConfigEntity>()
                .eq(LlmConfigEntity::getConfigType, configType)
                .eq(LlmConfigEntity::getConfigKey, configKey)
                .eq(LlmConfigEntity::getDeleted, false)
                .set(LlmConfigEntity::getEnabled, false)
                .set(LlmConfigEntity::getModifier, modifier);
        llmConfigMapper.update(null, wrapper);
        log.info("配置停用成功: {}.{}", configType, configKey);
    }

    /**
     * 停用同类型下所有指定模型的配置
     *
     * @param configType 配置类型
     * @param modelName 模型名称
     * @param modifier 修改人
     */
    private void disableAllByModelName(String configType, String modelName, String modifier) {
        List<LlmConfigEntity> enabledConfigs = llmConfigMapper.selectList(
                new LambdaQueryWrapper<LlmConfigEntity>()
                        .eq(LlmConfigEntity::getConfigType, configType)
                        .eq(LlmConfigEntity::getEnabled, true)
                        .eq(LlmConfigEntity::getDeleted, false)
                        .like(LlmConfigEntity::getConfigKey, modelName)
        );
        for (LlmConfigEntity config : enabledConfigs) {
            config.setEnabled(false);
            config.setModifier(modifier);
            llmConfigMapper.updateById(config);
            log.info("配置自动停用: {}.{}", configType, config.getConfigKey());
        }
    }

    /**
     * 从配置键中提取模型名称
     * 例如：dashscope_apiKey -> dashscope
     *
     * @param configKey 配置键
     * @return 模型名称，不含后缀
     */
    private String extractModelName(String configKey) {
        if (configKey == null) {
            return null;
        }
        int underscoreIdx = configKey.lastIndexOf('_');
        return underscoreIdx > 0 ? configKey.substring(0, underscoreIdx) : null;
    }
}