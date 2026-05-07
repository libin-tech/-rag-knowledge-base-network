package com.bin.ragknowledge.controller;

import com.bin.ragknowledge.repository.entity.KnowledgeBaseEntity;
import com.bin.ragknowledge.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库控制器
 * <p>
 * 提供知识库的增删改查API，支持后管页面切换知识库进行管理。
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/admin/api/knowledge-base")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 获取所有知识库列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<KnowledgeBaseEntity>> list() {
        return ResponseEntity.ok(knowledgeBaseService.listEnabled());
    }

    /**
     * 获取所有知识库（包括禁用的）
     */
    @GetMapping("/all")
    public ResponseEntity<List<KnowledgeBaseEntity>> listAll() {
        return ResponseEntity.ok(knowledgeBaseService.listAll());
    }

    /**
     * 根据ID获取知识库
     */
    @GetMapping("/{id}")
    public ResponseEntity<KnowledgeBaseEntity> getById(@PathVariable String id) {
        KnowledgeBaseEntity entity = knowledgeBaseService.getById(id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(entity);
    }

    /**
     * 获取默认知识库
     */
    @GetMapping("/default")
    public ResponseEntity<KnowledgeBaseEntity> getDefault() {
        return ResponseEntity.ok(knowledgeBaseService.getDefault());
    }

    /**
     * 创建知识库
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody KnowledgeBaseEntity entity) {
        Map<String, Object> result = new HashMap<>();
        try {
            KnowledgeBaseEntity saved = knowledgeBaseService.save(entity);
            result.put("success", true);
            result.put("data", saved);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * 更新知识库
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String id, @RequestBody KnowledgeBaseEntity entity) {
        Map<String, Object> result = new HashMap<>();
        try {
            entity.setId(id);
            knowledgeBaseService.updateById(entity);
            result.put("success", true);
            result.put("data", entity);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        } catch (Exception e) {
            log.error("更新知识库失败: id={}", id, e);
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(result);
        }
    }

    /**
     * 检查知识库是否可删除（检查是否存在相关数据）
     */
    @GetMapping("/{id}/check-delete")
    public ResponseEntity<Map<String, Object>> checkDelete(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        
        if ("default".equals(id)) {
            result.put("success", false);
            result.put("message", "默认知识库不可删除");
            return ResponseEntity.ok(result);
        }

        long documentCount = knowledgeBaseService.countDocuments(id);
        boolean hasVectorData = knowledgeBaseService.hasVectorData(id);
        
        result.put("canDelete", documentCount == 0 && !hasVectorData);
        result.put("hasDocuments", documentCount > 0);
        result.put("hasVectorData", hasVectorData);
        
        return ResponseEntity.ok(result);
    }

    /**
     * 删除知识库
     * @param id 知识库ID
     * @param deleteRelated 是否级联删除相关数据
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable String id,
            @RequestParam(defaultValue = "false") boolean deleteRelated) {
        
        Map<String, Object> result = new HashMap<>();
        
        if ("default".equals(id)) {
            result.put("success", false);
            result.put("message", "默认知识库不可删除");
            return ResponseEntity.badRequest().body(result);
        }
        
        Map<String, Object> deleteResult = knowledgeBaseService.deleteById(id, deleteRelated);
        
        if ((Boolean) deleteResult.get("success")) {
            return ResponseEntity.ok(deleteResult);
        } else {
            return ResponseEntity.badRequest().body(deleteResult);
        }
    }

    /**
     * 检查知识库是否存在
     */
    @GetMapping("/exists/{id}")
    public ResponseEntity<Map<String, Boolean>> exists(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("exists", knowledgeBaseService.exists(id)));
    }

    /**
     * 检查知识库是否存在向量数据
     */
    @GetMapping("/{id}/has-vector-data")
    public ResponseEntity<Map<String, Object>> hasVectorData(@PathVariable String id) {
        Map<String, Object> result = new HashMap<>();
        result.put("hasVectorData", knowledgeBaseService.hasVectorData(id));
        return ResponseEntity.ok(result);
    }

    /**
     * 检查知识库名称是否已存在
     */
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Object>> checkName(@RequestParam String name, @RequestParam(required = false) String excludeId) {
        Map<String, Object> result = new HashMap<>();
        boolean exists = knowledgeBaseService.existsByName(name);
        
        // 如果提供了排除ID，且存在的知识库就是要排除的ID，则认为不重复
        if (exists && excludeId != null) {
            KnowledgeBaseEntity existing = knowledgeBaseService.getById(excludeId);
            if (existing != null && existing.getName().equals(name)) {
                exists = false;
            }
        }
        
        result.put("exists", exists);
        return ResponseEntity.ok(result);
    }
}