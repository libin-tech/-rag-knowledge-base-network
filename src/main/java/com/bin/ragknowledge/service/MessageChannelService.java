package com.bin.ragknowledge.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bin.ragknowledge.repository.entity.MessageChannelEntity;
import com.bin.ragknowledge.repository.mapper.MessageChannelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageChannelService {

    private final MessageChannelMapper messageChannelMapper;

    private final Map<String, MessageChannelEntity> channelCache = new ConcurrentHashMap<>();

    public List<MessageChannelEntity> listAll() {
        return messageChannelMapper.selectList(new LambdaQueryWrapper<MessageChannelEntity>()
                .eq(MessageChannelEntity::getDeleted, false)
                .orderByAsc(MessageChannelEntity::getChannelType));
    }

    public MessageChannelEntity getByType(String channelType) {
        return channelCache.computeIfAbsent(channelType, k -> messageChannelMapper.selectOne(new LambdaQueryWrapper<MessageChannelEntity>()
                .eq(MessageChannelEntity::getChannelType, channelType)
                .eq(MessageChannelEntity::getDeleted, false)));
    }

    public MessageChannelEntity getEnabledChannel(String channelType) {
        MessageChannelEntity entity = getByType(channelType);
        if (entity != null && Boolean.TRUE.equals(entity.getEnabled())) {
            return entity;
        }
        return null;
    }

    public Map<String, Object> getConfigJson(String channelType) {
        MessageChannelEntity entity = getByType(channelType);
        if (entity != null && entity.getConfigJson() != null) {
            return JSONUtil.toBean(entity.getConfigJson(), Map.class);
        }
        return null;
    }

    @Transactional
    public void updateChannel(String channelType, MessageChannelEntity updateEntity) {
        LambdaUpdateWrapper<MessageChannelEntity> wrapper = new LambdaUpdateWrapper<MessageChannelEntity>()
                .eq(MessageChannelEntity::getChannelType, channelType)
                .eq(MessageChannelEntity::getDeleted, false);
        
        messageChannelMapper.update(updateEntity, wrapper);
        
        channelCache.remove(channelType);
        log.info("消息渠道配置已更新: {}", channelType);
    }

    public void clearCache() {
        channelCache.clear();
        log.info("消息渠道缓存已清空");
    }
}