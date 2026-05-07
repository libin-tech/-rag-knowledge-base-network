package com.bin.ragknowledge.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.hutool.core.util.EnumUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bin.ragknowledge.enums.ChannelType;
import com.bin.ragknowledge.repository.entity.MessageChannelEntity;
import com.bin.ragknowledge.repository.mapper.MessageChannelMapper;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageChannelService {

    private final MessageChannelMapper messageChannelMapper;

    private final Map<String, MessageChannelEntity> channelCache = new ConcurrentHashMap<>();

    public List<MessageChannelEntity> listAll() {
        return messageChannelMapper.selectList(new LambdaQueryWrapper<MessageChannelEntity>()
                .orderByAsc(MessageChannelEntity::getChannelType));
    }

    public List<MessageChannelEntity> listByKnowledgeBaseId(String knowledgeBaseId) {
        return messageChannelMapper.selectList(new LambdaQueryWrapper<MessageChannelEntity>()
                .eq(MessageChannelEntity::getKnowledgeBaseId, knowledgeBaseId)
                .orderByAsc(MessageChannelEntity::getChannelType));
    }

    public MessageChannelEntity getByTypeAndKb(ChannelType channelType, String knowledgeBaseId) {
        String cacheKey = channelType.getCode() + ":" + knowledgeBaseId;
        return channelCache.computeIfAbsent(cacheKey, k -> messageChannelMapper.selectOne(new LambdaQueryWrapper<MessageChannelEntity>()
                .eq(MessageChannelEntity::getChannelType, channelType)
                .eq(MessageChannelEntity::getKnowledgeBaseId, knowledgeBaseId)));
    }

    public MessageChannelEntity getByType(ChannelType channelType) {
        return getByTypeAndKb(channelType, "default");
    }

    public MessageChannelEntity getEnabledChannel(ChannelType channelType, String knowledgeBaseId) {
        MessageChannelEntity entity = getByTypeAndKb(channelType, knowledgeBaseId);
        if (entity != null && Boolean.TRUE.equals(entity.getEnabled())) {
            return entity;
        }
        return null;
    }

    public MessageChannelEntity getEnabledChannel(ChannelType channelType) {
        return getEnabledChannel(channelType, "default");
    }

    public Map<String, Object> getConfigJson(ChannelType channelType, String knowledgeBaseId) {
        MessageChannelEntity entity = getByTypeAndKb(channelType, knowledgeBaseId);
        if (entity != null && entity.getConfigJson() != null) {
            return JSONUtil.toBean(entity.getConfigJson(), Map.class);
        }
        return null;
    }

    public Map<String, Object> getConfigJson(ChannelType channelType) {
        return getConfigJson(channelType, "default");
    }

    @Transactional
    public void updateChannel(ChannelType channelType, String knowledgeBaseId, MessageChannelEntity updateEntity) {
        if (channelType == null ) {
            throw new IllegalArgumentException("渠道类型不能为空");
        }

        if (knowledgeBaseId == null || knowledgeBaseId.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }

        if (updateEntity == null) {
            throw new IllegalArgumentException("更新实体不能为空");
        }

        try {
            LambdaUpdateWrapper<MessageChannelEntity> wrapper = new LambdaUpdateWrapper<MessageChannelEntity>()
                    .eq(MessageChannelEntity::getChannelType, channelType)
                    .eq(MessageChannelEntity::getKnowledgeBaseId, knowledgeBaseId);


            int rows = messageChannelMapper.update(updateEntity, wrapper);

            String cacheKey = channelType + ":" + knowledgeBaseId;
            channelCache.remove(cacheKey);

            if (rows > 0) {
                log.info("消息渠道配置已更新: {}, knowledgeBaseId: {}", channelType, knowledgeBaseId);
            } else {
                log.warn("消息渠道配置更新失败，未找到匹配记录: {}, knowledgeBaseId: {}", channelType, knowledgeBaseId);
            }
        } catch (Exception e) {
            log.error("更新消息渠道配置失败: channelType={}, knowledgeBaseId={}, error={}", channelType, knowledgeBaseId, e.getMessage(), e);
            throw new RuntimeException("更新消息渠道配置失败: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void updateChannel(ChannelType channelType, MessageChannelEntity updateEntity) {
        updateChannel(channelType, "default", updateEntity);
    }

    @Transactional
    public void saveOrUpdateChannel(ChannelType channelType, String knowledgeBaseId, MessageChannelEntity entity) {
        if (channelType == null ) {
            throw new IllegalArgumentException("渠道类型不能为空");
        }
        if (knowledgeBaseId == null || knowledgeBaseId.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        if (entity == null) {
            throw new IllegalArgumentException("渠道实体不能为空");
        }

        try {

            LambdaQueryWrapper<MessageChannelEntity> queryWrapper = new LambdaQueryWrapper<MessageChannelEntity>()
                    .eq(MessageChannelEntity::getChannelType, channelType)
                    .eq(MessageChannelEntity::getKnowledgeBaseId, knowledgeBaseId);
            MessageChannelEntity existing = messageChannelMapper.selectOne(queryWrapper);

            if (existing != null) {
                entity.setId(existing.getId());
                entity.setChannelType(channelType);
                entity.setChannelName(channelType.getDesc());
                entity.setCreateTime(existing.getCreateTime());
                entity.setCreator(existing.getCreator());
                entity.setUpdateTime(LocalDateTime.now());
                messageChannelMapper.updateById(entity);
                log.info("消息渠道已更新: channelType={}, knowledgeBaseId={}, id={}",
                        channelType, knowledgeBaseId, existing.getId());
            } else {
                entity.setId(IdUtil.randomUUID());
                entity.setChannelType(channelType);
                entity.setChannelName(channelType.getDesc());
                entity.setKnowledgeBaseId(knowledgeBaseId);
                String creator = entity.getModifier() != null ? entity.getModifier() : "system";
                entity.setCreator(creator);
                entity.setCreateTime(LocalDateTime.now());
                entity.setUpdateTime(LocalDateTime.now());
                messageChannelMapper.insert(entity);
                log.info("消息渠道已创建: channelType={}, knowledgeBaseId={}, id={}",
                        channelType, knowledgeBaseId, entity.getId());
            }

            String cacheKey = channelType + ":" + knowledgeBaseId;
            channelCache.remove(cacheKey);
        } catch (Exception e) {
            log.error("保存或更新消息渠道失败: channelType={}, knowledgeBaseId={}, error={}",
                    channelType, knowledgeBaseId, e.getMessage(), e);
            throw new RuntimeException("保存或更新消息渠道失败: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void saveOrUpdateChannel(ChannelType channelType, MessageChannelEntity entity) {
        saveOrUpdateChannel(channelType, "default", entity);
    }

    @Transactional
    public void initChannelsForKnowledgeBase(String knowledgeBaseId, String creator) {
        if (knowledgeBaseId == null || knowledgeBaseId.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }

        if (creator == null || creator.trim().isEmpty()) {
            throw new IllegalArgumentException("创建人不能为空");
        }

        try {
            purgeDeletedRecords(knowledgeBaseId);

            MessageChannelEntity feishuChannel = new MessageChannelEntity();
            feishuChannel.setId(cn.hutool.core.util.IdUtil.randomUUID());
            feishuChannel.setChannelType(ChannelType.FEISHU);
            feishuChannel.setChannelName("飞书机器人");
            feishuChannel.setEnabled(false);
            feishuChannel.setKnowledgeBaseId(knowledgeBaseId);
            feishuChannel.setCreator(creator);
            feishuChannel.setModifier(creator);
            feishuChannel.setCreateTime(LocalDateTime.now());
            feishuChannel.setUpdateTime(LocalDateTime.now());
            messageChannelMapper.insert(feishuChannel);

            MessageChannelEntity dingtalkChannel = new MessageChannelEntity();
            dingtalkChannel.setId(cn.hutool.core.util.IdUtil.randomUUID());
            dingtalkChannel.setChannelType(ChannelType.DINGTALK);
            dingtalkChannel.setChannelName("钉钉机器人");
            dingtalkChannel.setEnabled(false);
            dingtalkChannel.setKnowledgeBaseId(knowledgeBaseId);
            dingtalkChannel.setCreator(creator);
            dingtalkChannel.setModifier(creator);
            dingtalkChannel.setCreateTime(LocalDateTime.now());
            dingtalkChannel.setUpdateTime(LocalDateTime.now());
            messageChannelMapper.insert(dingtalkChannel);

            MessageChannelEntity wechatChannel = new MessageChannelEntity();
            wechatChannel.setId(cn.hutool.core.util.IdUtil.randomUUID());
            wechatChannel.setChannelType(ChannelType.WECHAT_WORK);
            wechatChannel.setChannelName("企业微信机器人");
            wechatChannel.setEnabled(false);
            wechatChannel.setKnowledgeBaseId(knowledgeBaseId);
            wechatChannel.setCreator(creator);
            wechatChannel.setModifier(creator);
            wechatChannel.setCreateTime(LocalDateTime.now());
            wechatChannel.setUpdateTime(LocalDateTime.now());
            messageChannelMapper.insert(wechatChannel);

            log.info("知识库 {} 已初始化消息渠道", knowledgeBaseId);
        } catch (Exception e) {
            log.error("为知识库初始化消息渠道失败: knowledgeBaseId={}, error={}", knowledgeBaseId, e.getMessage(), e);
            throw new RuntimeException("初始化消息渠道失败: " + e.getMessage(), e);
        }
    }

    private void purgeDeletedRecords(String knowledgeBaseId) {
        Wrapper<MessageChannelEntity> wrapper = new LambdaQueryWrapper<MessageChannelEntity>()
                .eq(MessageChannelEntity::getKnowledgeBaseId, knowledgeBaseId);
        int deletedCount = messageChannelMapper.delete(wrapper);
        if (deletedCount > 0) {
            log.info("已物理清理知识库 {} 下的 {} 条已删除消息渠道记录", knowledgeBaseId, deletedCount);
        }
    }

    public long countByKnowledgeBaseId(String knowledgeBaseId) {
        return messageChannelMapper.selectCount(new LambdaQueryWrapper<MessageChannelEntity>()
                .eq(MessageChannelEntity::getKnowledgeBaseId, knowledgeBaseId));
    }

    @Transactional
    public void deleteByKnowledgeBaseId(String knowledgeBaseId) {
        Wrapper<MessageChannelEntity> wrapper = new LambdaQueryWrapper<MessageChannelEntity>()
                .eq(MessageChannelEntity::getKnowledgeBaseId, knowledgeBaseId);
        messageChannelMapper.delete(wrapper);

        log.info("知识库 {} 的消息渠道已彻底删除", knowledgeBaseId);
    }

    public void clearCache() {
        channelCache.clear();
        log.info("消息渠道缓存已清空");
    }
}
