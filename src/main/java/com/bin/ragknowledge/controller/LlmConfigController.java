package com.bin.ragknowledge.controller;

import com.bin.ragknowledge.config.EmbeddingModelFactory;
import com.bin.ragknowledge.config.ModelFactory;
import com.bin.ragknowledge.service.LlmConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/api/config")
@RequiredArgsConstructor
public class LlmConfigController {

    private final LlmConfigService llmConfigService;
    private final ModelFactory modelFactory;
    private final EmbeddingModelFactory embeddingModelFactory;

    @GetMapping("/llm")
    public ResponseEntity<Map<String, Object>> getLlmConfig() {
        try {
            Map<String, String> configMap = llmConfigService.getConfigMap("LLM");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", configMap
            ));
        } catch (Exception e) {
            log.error("获取LLM配置失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "获取配置失败: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/embedding")
    public ResponseEntity<Map<String, Object>> getEmbeddingConfig() {
        try {
            Map<String, String> configMap = llmConfigService.getConfigMap("EMBEDDING");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", configMap
            ));
        } catch (Exception e) {
            log.error("获取Embedding配置失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "获取配置失败: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/llm")
    public ResponseEntity<Map<String, Object>> updateLlmConfig(@RequestBody ConfigUpdateRequest request) {
        try {
            llmConfigService.updateConfig("LLM", request.getConfigKey(), request.getConfigValue(), request.getModifier());
            modelFactory.refreshChatModels();
            log.info("LLM配置已更新并刷新模型缓存");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "配置更新成功，模型已重新加载"
            ));
        } catch (Exception e) {
            log.error("更新LLM配置失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "更新配置失败: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/embedding")
    public ResponseEntity<Map<String, Object>> updateEmbeddingConfig(@RequestBody ConfigUpdateRequest request) {
        try {
            llmConfigService.updateConfig("EMBEDDING", request.getConfigKey(), request.getConfigValue(), request.getModifier());
            embeddingModelFactory.refreshEmbeddingModel();
            log.info("Embedding配置已更新并刷新模型缓存");
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "配置更新成功，模型已重新加载"
            ));
        } catch (Exception e) {
            log.error("更新Embedding配置失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "更新配置失败: " + e.getMessage()
            ));
        }
    }

    @lombok.Data
    public static class ConfigUpdateRequest {
        private String configKey;
        private String configValue;
        private String modifier;
    }
}