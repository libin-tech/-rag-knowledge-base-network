package com.bin.ragknowledge.config;

import com.bin.ragknowledge.enums.EmbeddingMode;
import com.bin.ragknowledge.service.LlmConfigService;
import dev.langchain4j.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmbeddingModelFactory {

    private final LlmConfigService llmConfigService;

    private EmbeddingModel embeddingModelCache;

    @PostConstruct
    public void init() {
        refreshEmbeddingModel();
        log.info("Embedding模型工厂初始化完成");
    }

    public void refreshEmbeddingModel() {
        embeddingModelCache = null;
        log.info("Embedding模型缓存已刷新");
    }

    public EmbeddingModel getEmbeddingModel() {
        if (embeddingModelCache == null) {
            EmbeddingConfig config = llmConfigService.getEmbeddingConfig();
            String mode = config.getMode();
            log.info("创建 EmbeddingModel, mode: {}", mode);
            embeddingModelCache = createEmbeddingModel(mode, config);
        }
        return embeddingModelCache;
    }

    private EmbeddingModel createEmbeddingModel(String mode, EmbeddingConfig config) {
        return switch (mode.toLowerCase()) {
            case EmbeddingMode.OLLAMA -> OllamaEmbeddingModel.builder()
                    .baseUrl(config.getOllamaBaseUrl())
                    .modelName(config.getOllamaModelName())
                    .timeout(Duration.parse("PT" + config.getOllamaTimeout().toUpperCase()))
                    .build();
            default -> QwenEmbeddingModel.builder()
                    .apiKey(config.getDashscopeApiKey())
                    .modelName(config.getDashscopeModelName())
                    .build();
        };
    }
}