package com.bin.ragknowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bin.ragknowledge.repository.entity.DocumentMetadataEntity;
import com.bin.ragknowledge.repository.mapper.DocumentMetadataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentMetadataService {

    private final DocumentMetadataMapper documentMetadataMapper;

    public boolean save(DocumentMetadataEntity entity) {
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return documentMetadataMapper.insert(entity) > 0;
    }

    public boolean updateById(DocumentMetadataEntity entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return documentMetadataMapper.updateById(entity) > 0;
    }

    public boolean deleteById(String id) {
        return documentMetadataMapper.deleteById(id) > 0;
    }

    public DocumentMetadataEntity getById(String id) {
        return documentMetadataMapper.selectById(id);
    }

    public List<DocumentMetadataEntity> listAll() {
        return documentMetadataMapper.selectList(null);
    }

    public Page<DocumentMetadataEntity> page(int current, int size) {
        Page<DocumentMetadataEntity> page = new Page<>(current, size);
        QueryWrapper<DocumentMetadataEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("upload_time");
        return documentMetadataMapper.selectPage(page, queryWrapper);
    }
}