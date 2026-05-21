package com.bintech.rag.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bintech.rag.repository.entity.DocumentMetadataEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DocumentMetadataMapper extends BaseMapper<DocumentMetadataEntity> {
}