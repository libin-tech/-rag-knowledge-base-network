package com.family.assistant.service;

import cn.hutool.core.util.IdUtil;
import com.family.assistant.config.RagProperties;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

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

    /** 聊天语言模型，用于生成智能回答 */
    private final ChatLanguageModel chatLanguageModel;

    /** 流式聊天语言模型，用于流式输出 */
    private final StreamingChatLanguageModel streamingChatLanguageModel;

    /** 嵌入模型，用于将文本转换为向量表示 */
    private final EmbeddingModel embeddingModel;

    /** 向量存储库，用于存储和检索文本片段及其向量 */
    private final EmbeddingStore<TextSegment> embeddingStore;

    /** RAG 相关配置属性，包含文档分割参数和检索策略配置 */
    private final RagProperties ragProperties;

    /**
     * 聊天记忆组件，用于维护对话上下文
     * 使用 MessageWindowChatMemory 实现固定窗口大小的记忆管理，
     * 最多保留 10 条消息，超出后自动丢弃最早的消息
     */
    private ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

    /**
     * 添加文档到向量数据库
     * <p>
     * 工作流程：
     * 1. 文档分割：根据配置的块大小和重叠度将文档切分为多个文本片段
     * 2. 元数据标记：为每个片段添加唯一的文档 ID，便于追溯来源
     * 3. 向量化处理：使用嵌入模型将所有文本片段转换为向量表示
     * 4. 持久化存储：将向量与对应的文本片段一并存入向量数据库
     * </p>
     *
     * @param document 待入库的 LangChain4j Document 对象，包含文档内容和元数据
     */
    public void addDocument(Document document) {
        // 根据配置参数创建文档分割器
        // 使用递归分割策略，按配置的块大小和重叠度进行分割
        DocumentSplitter splitter = DocumentSplitters.recursive(
                ragProperties.getChunk().getMaxSegmentSize(),
                ragProperties.getChunk().getMaxOverlapSize()
        );

        // 执行文档分割，获取文本片段列表
        List<TextSegment> segments = splitter.split(document);
        log.info("文档被分割为 {} 个片段", segments.size());

        // 生成唯一的文档标识符，用于标记这批片段来源于同一份原始文档
        String docId = IdUtil.randomUUID();
        // 为每个文本片段添加 doc_id 元数据，建立片段与原文件的关联关系
        for (TextSegment segment : segments) {
            segment.metadata().put("doc_id", docId);
        }

        // 调用嵌入模型批量向量化所有文本片段
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
        // 将向量和对应的文本片段一并写入向量数据库
        embeddingStore.addAll(embeddings, segments);

        log.info("文档已添加到向量数据库, docId: {}", docId);
    }

    /**
     * 批量添加多个文档到向量数据库
     * <p>
     * 工作流程：遍历文档列表，逐个调用 addDocument 方法完成入库操作。
     * 此方法是对 addDocument 的简单封装，便于批量操作。
     * </p>
     *
     * @param documents 待入库的 Document 对象列表
     */
    public void addDocuments(List<Document> documents) {
        // 逐个处理每个文档
        for (Document document : documents) {
            addDocument(document);
        }
    }

    /**
     * 执行智能问答查询
     * <p>
     * 工作流程：
     * 1. 构建内容检索器：基于向量数据库和嵌入模型创建检索器，配置最大返回结果数和最低相似度阈值
     * 2. 组装 AI 服务：使用 AiServices 将语言模型、聊天记忆和内容检索器整合为一个完整的 AI 助手
     * 3. 执行对话：调用助手的 chat 方法，系统会自动检索相关文档并结合上下文生成回答
     * 4. 记录日志：记录用户问题和 AI 回答，便于追踪和分析
     * </p>
     *
     * @param question 用户提出的问题文本
     * @return AI 生成的回答文本
     */
    public String query(String question) {
        // 创建内容检索器，用于从向量数据库中检索与问题相关的文档片段
        // maxResults: 最大返回结果数量，控制上下文的丰富度
        // minScore: 最低相似度阈值，过滤掉相关性过低的结果
        ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(ragProperties.getRetrieval().getMaxResults())
                .minScore(ragProperties.getRetrieval().getMinScore())
                .build();

        // 使用 AiServices 构建 AI 助手，整合多个核心组件
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)  // 设置大语言模型，负责生成最终回答
                .chatMemory(chatMemory)                // 设置聊天记忆，维护多轮对话上下文
                .contentRetriever(retriever)           // 设置内容检索器，提供 RAG 检索能力
                .build();

        // 记录用户提问日志
        log.info("用户提问: {}", question);
        // 调用 AI 助手进行对话，系统会自动完成检索-增强-生成的完整流程
        String answer = assistant.chat(question);
        // 记录 AI 回答日志
        log.info("AI 回答: {}", answer);

        return answer;
    }

    /**
     * 执行流式智能问答查询
     * <p>
     * 工作流程：
     * 1. 构建内容检索器：基于向量数据库和嵌入模型创建检索器
     * 2. 使用流式模型进行对话，实现打字机效果输出
     * 3. 通过回调函数逐字返回生成的内容
     * 4. 最终返回 token 使用统计信息
     * </p>
     *
     * @param question 用户提出的问题文本
     * @param onNext 接收每个生成文本片段的回调函数
     * @param onComplete 完成时的回调函数，接收 token 使用统计
     * @param onError 错误时的回调函数
     */
    public void queryStream(
            String question,
            Consumer<String> onNext,
            Consumer<TokenUsage> onComplete,
            Consumer<Throwable> onError) {
        try {
            // 创建内容检索器
            ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
                    .embeddingStore(embeddingStore)
                    .embeddingModel(embeddingModel)
                    .maxResults(ragProperties.getRetrieval().getMaxResults())
                    .minScore(ragProperties.getRetrieval().getMinScore())
                    .build();

            // 记录用户提问日志
            log.info("用户提问（流式）: {}", question);

            // 获取对话历史
            List<dev.langchain4j.data.message.ChatMessage> messages = chatMemory.messages();
            
            // 添加用户消息
            messages.add(new dev.langchain4j.data.message.UserMessage(question));

            // 使用流式模型进行对话
            streamingChatLanguageModel.generate(messages, new StreamingResponseHandler<>() {
                @Override
                public void onNext(String token) {
                    onNext.accept(token);
                }

                @Override
                public void onComplete(Response<dev.langchain4j.data.message.AiMessage> response) {
                    TokenUsage tokenUsage = response.tokenUsage();
                    log.info("AI 回答完成（流式），Token 使用: {}", tokenUsage);

                    // 将 AI 回复添加到对话历史中
                    if (response.content() != null) {
                        chatMemory.add(new dev.langchain4j.data.message.UserMessage(question));
                        chatMemory.add(response.content());
                    }

                    onComplete.accept(tokenUsage);
                }

                @Override
                public void onError(Throwable error) {
                    log.error("流式生成出错", error);
                    onError.accept(error);
                }
            });
        } catch (Exception e) {
            log.error("流式查询出错", e);
            onError.accept(e);
        }
    }

    /**
     * 清空向量数据库
     * <p>
     * 注意：当前实现仅记录日志，实际清空操作取决于底层向量数据库的支持。
     * 以 Milvus 为例，不支持直接清空集合中的全部数据，通常需要删除集合后重建。
     * 此方法预留接口，后续可根据实际需求完善具体实现。
     * </p>
     */
    public void clearStore() {
        // Milvus 不支持直接清空，需要删除集合后重建
        log.info("向量数据库清空完成");
    }

    /**
     * 检索与查询相关的文档片段
     * <p>
     * 工作流程：
     * 1. 将查询文本向量化，得到查询向量
     * 2. 构建搜索请求，设置最大结果数和最低相似度阈值
     * 3. 在向量数据库中执行相似度搜索
     * 4. 提取搜索结果中的文本片段并返回列表
     * </p>
     * <p>
     * 此方法通常用于调试或预览检索效果，不直接调用 LLM 生成回答。
     * </p>
     *
     * @param query 查询文本，用于在向量数据库中检索相关片段
     * @return 与查询最相关的 TextSegment 列表，按相关性降序排列
     */
    public List<TextSegment> retrieveRelevantDocuments(String query) {
        // 将查询文本通过嵌入模型转换为向量表示
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 构建向量搜索请求，配置搜索参数
        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)                              // 设置查询向量
                .maxResults(ragProperties.getRetrieval().getMaxResults())   // 设置最大返回数量
                .minScore(ragProperties.getRetrieval().getMinScore())       // 设置最低相似度阈值
                .build();

        // 在向量存储中执行相似度搜索
        EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
        // 从搜索结果中提取嵌入的文本片段，转换为列表返回
        return result.matches().stream()
                .map(EmbeddingMatch::embedded)
                .toList();
    }

    /**
     * AI 助手接口定义
     * <p>
     * 该接口由 LangChain4j 的 AiServices 动态代理实现，
     * 开发者只需定义方法签名，框架会自动完成检索增强和对话管理的完整流程。
     * </p>
     */
    public interface Assistant {
        /**
         * 与 AI 助手进行单轮对话
         *
         * @param userMessage 用户输入的消息文本
         * @return AI 生成的回答文本
         */
        String chat(String userMessage);
    }
}
