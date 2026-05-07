package com.bin.ragknowledge.config;

import com.bin.ragknowledge.enums.LlmMode;
import com.bin.ragknowledge.service.LlmConfigService;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.dashscope.QwenChatModel;
import dev.langchain4j.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ModelFactory {

    private final LlmConfigService llmConfigService;

    private final Map<String, ChatLanguageModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<String, StreamingChatLanguageModel> streamingChatModelCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refreshChatModels();
        log.info("模型工厂初始化完成");
    }

    public void refreshChatModels() {
        chatModelCache.clear();
        streamingChatModelCache.clear();
        log.info("模型缓存已刷新");
    }

    public ChatLanguageModel getChatLanguageModel() {
        LlmConfig config = llmConfigService.getLlmConfig();
        String mode = config.getMode();
        String cacheKey = "chat:" + mode;

        return chatModelCache.computeIfAbsent(cacheKey, k -> {
            log.info("创建 ChatLanguageModel, mode: {}", mode);
            return createChatLanguageModel(mode, config);
        });
    }

    public StreamingChatLanguageModel getStreamingChatLanguageModel() {
        LlmConfig config = llmConfigService.getLlmConfig();
        String mode = config.getMode();
        String cacheKey = "streaming:" + mode;

        return streamingChatModelCache.computeIfAbsent(cacheKey, k -> {
            log.info("创建 StreamingChatLanguageModel, mode: {}", mode);
            return createStreamingChatLanguageModel(mode, config);
        });
    }

    private ChatLanguageModel createChatLanguageModel(String mode, LlmConfig config) {


        if (LlmMode.DASHSCOPE.getValue().equals(mode)) {
            return QwenChatModel.builder()
                    .apiKey(config.getDashscopeApiKey())
                    .modelName(config.getDashscopeModelName())
                    .build();
        }
        if (LlmMode.OLLAMA.getValue().equals(mode)) {
            return OllamaChatModel.builder()
                    .baseUrl(config.getOllamaBaseUrl())
                    .modelName(config.getOllamaModelName())
                    .timeout(Duration.parse("PT" + config.getOllamaTimeout().toUpperCase()))
                    .build();
        }
        if (LlmMode.OPENAI.getValue().equals(mode)) {
            return OpenAiChatModel.builder()
                    .apiKey(config.getOpenaiApiKey())
                    .baseUrl(config.getOpenaiBaseUrl())
                    .modelName(config.getOpenaiModelName())
                    .timeout(Duration.parse("PT" + config.getOpenaiTimeout().toUpperCase()))
                    .build();
        }

        throw new IllegalArgumentException("不支持的LLM模式：" + mode);

    }

    private StreamingChatLanguageModel createStreamingChatLanguageModel(String mode, LlmConfig config) {


        if (LlmMode.DASHSCOPE.getValue().equals(mode)) {
            return QwenStreamingChatModel.builder()
                    .apiKey(config.getDashscopeApiKey())
                    .modelName(config.getDashscopeModelName())
                    .build();
        }
        if (LlmMode.OLLAMA.getValue().equals(mode)) {
            return OllamaStreamingChatModel.builder()
                    .baseUrl(config.getOllamaBaseUrl())
                    .modelName(config.getOllamaModelName())
                    .timeout(Duration.parse("PT" + config.getOllamaTimeout().toUpperCase()))
                    .build();
        }
        if (LlmMode.OPENAI.getValue().equals(mode)) {
            return OpenAiStreamingChatModel.builder()
                    .apiKey(config.getOpenaiApiKey())
                    .baseUrl(config.getOpenaiBaseUrl())
                    .modelName(config.getOpenaiModelName())
                    .timeout(Duration.parse("PT" + config.getOpenaiTimeout().toUpperCase()))
                    .build();
        }

        throw new IllegalArgumentException("不支持的LLM模式：" + mode);
    }
}