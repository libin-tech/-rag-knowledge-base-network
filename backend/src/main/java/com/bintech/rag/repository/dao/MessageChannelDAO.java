package com.bintech.rag.repository.dao;

import com.bintech.rag.enums.ChannelType;
import com.bintech.rag.repository.entity.MessageChannelEntity;

import java.util.List;

public interface MessageChannelDAO {

    int insert(MessageChannelEntity entity);

    int updateById(MessageChannelEntity entity);

    int update(MessageChannelEntity entity, ChannelType channelType, String knowledgeBaseId);

    int deleteByKnowledgeBaseId(String knowledgeBaseId);

    List<MessageChannelEntity> selectAll();

    List<MessageChannelEntity> selectByKnowledgeBaseId(String knowledgeBaseId);

    MessageChannelEntity selectByTypeAndKb(ChannelType channelType, String knowledgeBaseId);

    long countByKnowledgeBaseId(String knowledgeBaseId);
}
