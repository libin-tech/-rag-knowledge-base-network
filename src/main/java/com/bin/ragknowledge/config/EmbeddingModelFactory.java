package com.bin.ragknowledge.config;

import com.bin.ragknowledge.enums.LlmMode;
import com.bin.ragknowledge.service.LlmConfigService;
import dev.langchain4j.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
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


        if (LlmMode.DASHSCOPE.getValue().equals(mode)){
            return QwenEmbeddingModel.builder()
                    .apiKey(config.getDashscopeApiKey())
                    .modelName(config.getDashscopeModelName())
                    .build();
        }
        if (LlmMode.OLLAMA.getValue().equals(mode)){
            return OllamaEmbeddingModel.builder()
                    .baseUrl(config.getOllamaBaseUrl())
                    .modelName(config.getOllamaModelName())
                    .timeout(Duration.parse("PT" + config.getOllamaTimeout().toUpperCase()))
                    .build();
        }
        if (LlmMode.OPENAI.getValue().equals(mode)){
            return OpenAiEmbeddingModel.builder()
                    .baseUrl(config.getOpenaiBaseUrl())
                    .apiKey(config.getOpenaiApiKey())
                    .modelName(config.getOpenaiModelName())
                    .timeout(Duration.parse("PT" + config.getOpenaiTimeout().toUpperCase()))
                    .build();
        }

        throw new IllegalArgumentException("不支持的LLM模式：" + mode);


    }
}