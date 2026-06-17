package com.bintech.rag.repository.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bintech.rag.enums.ChannelType;
import com.bintech.rag.repository.entity.MessageChannelEntity;
import com.bintech.rag.repository.mapper.MessageChannelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
class MessageChannelDAOImpl implements MessageChannelDAO {

    private final MessageChannelMapper messageChannelMapper;

    @Override
    public int insert(MessageChannelEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("消息渠道实体不能为空");
        }
        return messageChannelMapper.insert(entity);
    }

    @Override
    public int updateById(MessageChannelEntity entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("消息渠道实体及ID不能为空");
        }
        return messageChannelMapper.updateById(entity);
    }

    @Override
    public int update(MessageChannelEntity entity, ChannelType channelType, String knowledgeBaseId) {
        if (channelType == null) {
            throw new IllegalArgumentException("渠道类型不能为空");
        }
        if (knowledgeBaseId == null || knowledgeBaseId.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        return messageChannelMapper.update(entity, new LambdaUpdateWrapper<MessageChannelEntity>()
                .eq(MessageChannelEntity::getChannelType, channelType)
                .eq(MessageChannelEntity::getKnowledgeBaseId, knowledgeBaseId));
    }

    @Override
    public int deleteByKnowledgeBaseId(String knowledgeBaseId) {
        if (knowledgeBaseId == null || knowledgeBaseId.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        return messageChannelMapper.delete(new LambdaQueryWrapper<MessageChannelEntity>()
                .eq(MessageChannelEntity::getKnowledgeBaseId, knowledgeBaseId));
    }

    @Override
    public List<MessageChannelEntity> selectAll() {
        return messageChannelMapper.selectList(new LambdaQueryWrapper<MessageChannelEntity>()
                .orderByAsc(MessageChannelEntity::getChannelType));
    }

    @Override
    public List<MessageChannelEntity> selectByKnowledgeBaseId(String knowledgeBaseId) {
        if (knowledgeBaseId == null || knowledgeBaseId.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        return messageChannelMapper.selectList(new LambdaQueryWrapper<MessageChannelEntity>()
                .eq(MessageChannelEntity::getKnowledgeBaseId, knowledgeBaseId)
                .orderByAsc(MessageChannelEntity::getChannelType));
    }

    @Override
    public MessageChannelEntity selectByTypeAndKb(ChannelType channelType, String knowledgeBaseId) {
        if (channelType == null) {
            throw new IllegalArgumentException("渠道类型不能为空");
        }
        if (knowledgeBaseId == null || knowledgeBaseId.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        return messageChannelMapper.selectOne(new LambdaQueryWrapper<MessageChannelEntity>()
                .eq(MessageChannelEntity::getChannelType, channelType)
                .eq(MessageChannelEntity::getKnowledgeBaseId, knowledgeBaseId));
    }

    @Override
    public long countByKnowledgeBaseId(String knowledgeBaseId) {
        if (knowledgeBaseId == null || knowledgeBaseId.trim().isEmpty()) {
            throw new IllegalArgumentException("知识库ID不能为空");
        }
        return messageChannelMapper.selectCount(new LambdaQueryWrapper<MessageChannelEntity>()
                .eq(MessageChannelEntity::getKnowledgeBaseId, knowledgeBaseId));
    }
}
