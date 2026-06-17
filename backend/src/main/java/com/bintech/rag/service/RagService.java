package com.bintech.rag.service;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.IdUtil;
import com.bintech.rag.config.EmbeddingModelFactory;
import com.bintech.rag.config.ModelFactory;
import com.bintech.rag.config.RagProperties;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * RAG（检索增强生成）服务
 * <p>
 * 职责：作为系统的核心智能问答组件，负责文档的向量化存储、语义检索和智能问答。
 * RAG 系统通过将文档分割为片段、向量化后存储到向量数据库中，在用户提问时
 * 检索相关文档片段，并将其作为上下文提供给大语言模型，从而生成准确的回答。
 * </p>
 *
 * <p>核心工作流程：</p>
 * <ol>
 *   <li>文档入库：接收文档 -> 按策略分割为片段 -> 为片段生成向量 -> 存储到向量数据库</li>
 *   <li>智能问答：接收问题 -> 向量化问题 -> 在向量库中检索相关片段 -> 结合上下文调用 LLM 生成回答</li>
 * </ol>
 *
 * <p>依赖组件：</p>
 * <ul>
 *   <li>ChatLanguageModel：大语言模型，用于生成最终回答</li>
 *   <li>EmbeddingModel：嵌入模型，用于将文本转换为向量表示</li>
 *   <li>EmbeddingStore：向量数据库存储，用于存储和检索文本向量</li>
 *   <li>RagProperties：RAG 相关配置属性，控制分割策略和检索参数</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    /** 模型工厂，用于动态获取聊天语言模型 */
    private final ModelFactory modelFactory;

    /** 嵌入模型工厂，用于动态获取嵌入模型 */
    private final EmbeddingModelFactory embeddingModelFactory;

    /** 向量存储库，用于存储和检索文本片段及其向量 */
    private final EmbeddingStore<TextSegment> embeddingStore;

    /** RAG 相关配置属性，包含文档分割参数和检索策略配置 */
    private final RagProperties ragProperties;

    /**
     * 添加文档到向量数据库（默认知识库）
     */
    public DocumentVectorResult addDocument(Document document) {
        return addDocument(document, "default");
    }

    /**
     * 添加文档到向量数据库（指定知识库）
     * <p>
     * 工作流程：
     * 1. 文档分割：根据配置的块大小和重叠度将文档切分为多个文本片段
     * 2. 元数据标记：为每个片段添加唯一的文档 ID 和知识库 ID
     * 3. 向量化处理：使用嵌入模型将所有文本片段转换为向量表示
     * 4. 持久化存储：将向量与对应的文本片段一并存入向量数据库
     * </p>
     *
     * @param document 待入库的 LangChain4j Document 对象，包含文档内容和元数据
     * @param knowledgeBaseId 知识库ID
     */
    public DocumentVectorResult addDocument(Document document, String knowledgeBaseId) {
        DocumentSplitter splitter = DocumentSplitters.recursive(
                ragProperties.getChunk().getMaxSegmentSize(),
                ragProperties.getChunk().getMaxOverlapSize()
        );

        List<TextSegment> segments = splitter.split(document);
        log.info("文档被分割为 {} 个片段", segments.size());

        String docId = IdUtil.randomUUID();
        for (TextSegment segment : segments) {
            segment.metadata().put("doc_id", docId);
            segment.metadata().put("knowledge_base_id", knowledgeBaseId);
        }

        List<Embedding> embeddings = embeddingModelFactory.getEmbeddingModel().embedAll(segments).content();
        List<String> vectorIds = embeddingStore.addAll(embeddings, segments);

        log.info("文档已添加到向量数据库, docId: {}, knowledgeBaseId: {}", docId, knowledgeBaseId);
        return new DocumentVectorResult(docId, segments.size(), vectorIds);
    }

    /**
     * 批量添加多个文档到向量数据库（默认知识库）
     */
    public void addDocuments(List<Document> documents) {
        addDocuments(documents, "default");
    }

    /**
     * 批量添加多个文档到向量数据库（指定知识库）
     */
    public void addDocuments(List<Document> documents, String knowledgeBaseId) {
        for (Document document : documents) {
            addDocument(document, knowledgeBaseId);
        }
    }

    public void deleteDocumentVectors(List<String> vectorIds) {
        if (vectorIds == null || vectorIds.isEmpty()) {
            return;
        }
        embeddingStore.removeAll(vectorIds);
        log.info("删除向量数据成功, 向量数: {}", vectorIds.size());
    }

    /**
     * 执行智能问答查询（默认知识库）
     */
    public String query(String question) {
        return query(question, "default");
    }

    /**
     * 执行智能问答查询（指定知识库）
     * <p>
     * 工作流程：
     * 1. 构建内容检索器：基于向量数据库和嵌入模型创建检索器，配置最大返回结果数和最低相似度阈值
     * 2. 组装 AI 服务：使用 AiServices 将语言模型、聊天记忆和内容检索器整合为一个完整的 AI 助手
     * 3. 执行对话：调用助手的 chat 方法，系统会自动检索相关文档并结合上下文生成回答
     * 4. 记录日志：记录用户问题和 AI 回答，便于追踪和分析
     * </p>
     *
     * @param question 用户提出的问题文本
     * @param knowledgeBaseId 知识库ID
     * @return AI 生成的回答文本
     */
    public String query(String question, String knowledgeBaseId) {
        var chatLanguageModel = modelFactory.getChatLanguageModel();
        var embeddingModel = embeddingModelFactory.getEmbeddingModel();

        Filter metadataFilter = MetadataFilterBuilder.metadataKey("knowledge_base_id")
                .isEqualTo(knowledgeBaseId);

        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(ragProperties.getRetrieval().getMaxResults())
                .minScore(ragProperties.getRetrieval().getMinScore())
                .filter(metadataFilter)
                .build();

        ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .contentRetriever(retriever)
                .build();

        log.info("用户提问: {}, 知识库: {}", question, knowledgeBaseId);
        log.info("正在增强检索，请耐心等待...");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start("rag-query");

        String answer = assistant.chatWithContext(question, knowledgeBaseId);

        stopWatch.stop();
        long costTime = stopWatch.getTotalTimeMillis();
        log.info("RAG 检索增强与回答耗时: {} ms", costTime);
        log.info("AI 回答: {}", answer);

        return answer;
    }

    /**
     * 执行流式智能问答查询（默认知识库）
     */
    public void queryStream(
            String question,
            Consumer<String> onNext,
            Consumer<TokenUsage> onComplete,
            Consumer<Throwable> onError) {
        queryStream(question, "default", onNext, onComplete, onError);
    }

    /**
     * 执行流式智能问答查询（指定知识库）
     */
    public void queryStream(
            String question,
            String knowledgeBaseId,
            Consumer<String> onNext,
            Consumer<TokenUsage> onComplete,
            Consumer<Throwable> onError) {
        try {
            var streamingChatLanguageModel = modelFactory.getStreamingChatLanguageModel();
            var embeddingModel = embeddingModelFactory.getEmbeddingModel();

            Filter metadataFilter = MetadataFilterBuilder.metadataKey("knowledge_base_id")
                    .isEqualTo(knowledgeBaseId);

            ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .maxResults(ragProperties.getRetrieval().getMaxResults())
                    .minScore(ragProperties.getRetrieval().getMinScore())
                    .filter(metadataFilter)
                    .build();

            log.info("用户提问（流式）: {}, 知识库: {}", question, knowledgeBaseId);

            ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

            StreamingAssistant streamingAssistant = AiServices.builder(StreamingAssistant.class)
                    .streamingChatLanguageModel(streamingChatLanguageModel)
                    .chatMemory(chatMemory)
                    .contentRetriever(retriever)
                    .build();

            TokenStream tokenStream = streamingAssistant.chatWithContext(question, knowledgeBaseId);

            tokenStream.onNext(onNext).onComplete((response) -> {
                TokenUsage tokenUsage = response.tokenUsage();
                log.info("AI 回答完成（流式），Token 使用: {}", tokenUsage);
                log.info("Token 详情 - inputTokenCount: {}, outputTokenCount: {}, totalTokenCount: {}",
                        tokenUsage != null ? tokenUsage.inputTokenCount() : "null",
                        tokenUsage != null ? tokenUsage.outputTokenCount() : "null",
                        tokenUsage != null ? tokenUsage.totalTokenCount() : "null");
                if (tokenUsage != null) {
                    onComplete.accept(tokenUsage);
                } else {
                    log.warn("tokenUsage 为 null，使用默认值");
                    TokenUsage defaultUsage = new TokenUsage(0, 0, 0);
                    onComplete.accept(defaultUsage);
                }
            }).onError((error) -> {
                log.error("流式生成出错", error);
                onError.accept(error);
            }).start();
        } catch (Exception e) {
            log.error("流式查询出错", e);
            onError.accept(e);
        }
    }

    public void clearStore() {
        log.info("向量数据库清空完成");
    }

    /**
     * 检索与查询相关的文档片段（默认知识库）
     */
    public List<TextSegment> retrieveRelevantDocuments(String query) {
        return retrieveRelevantDocuments(query, "default");
    }

    /**
     * 检索与查询相关的文档片段（指定知识库）
     */
    public List<TextSegment> retrieveRelevantDocuments(String query, String knowledgeBaseId) {
        var embeddingModel = embeddingModelFactory.getEmbeddingModel();

        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 构建元数据过滤器
        var filter = MetadataFilterBuilder.metadataKey("knowledge_base_id")
                .isEqualTo(knowledgeBaseId);

        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(ragProperties.getRetrieval().getMaxResults())
                .minScore(ragProperties.getRetrieval().getMinScore())
                .filter(filter)
                .build();

        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);

        return result.matches().stream()
                .map(EmbeddingMatch::embedded)
                .collect(Collectors.toList());
    }

    /**
     * AI 助手接口定义
     */
    public interface Assistant {
        String chat(String userMessage);

        @dev.langchain4j.service.SystemMessage("你是一个智能知识库问答助手。请根据提供的上下文信息回答用户的问题。如果没有相关信息，请明确说明。")
        @dev.langchain4j.service.UserMessage("知识库ID: {{knowledgeBaseId}}\n问题: {{question}}")
        String chatWithContext(@dev.langchain4j.service.V("question") String question,
                               @dev.langchain4j.service.V("knowledgeBaseId") String knowledgeBaseId);
    }

    /**
     * 流式 AI 助手接口定义
     */
    public interface StreamingAssistant {
        TokenStream chat(String userMessage);

        @dev.langchain4j.service.SystemMessage("你是一个智能知识库问答助手。请根据提供的上下文信息回答用户的问题。如果没有相关信息，请明确说明。")
        @dev.langchain4j.service.UserMessage("知识库ID: {{knowledgeBaseId}}\n问题: {{question}}")
        TokenStream chatWithContext(@dev.langchain4j.service.V("question") String question,
                                    @dev.langchain4j.service.V("knowledgeBaseId") String knowledgeBaseId);
    }

    public record DocumentVectorResult(String vectorDocId, int segmentCount, List<String> vectorIds) {
    }
}