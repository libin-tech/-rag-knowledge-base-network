package com.bin.ragknowledge.controller;

import com.bin.ragknowledge.service.DocumentService;
import com.bin.ragknowledge.service.RagService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.model.output.TokenUsage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理后台控制器
 * 负责处理管理后台的所有请求，包括文档上传、批量上传、问答测试、文档管理等功能。
 * 该控制器提供页面路由和 RESTful API 接口，支持单个/批量 PDF 文件上传至向量数据库，
 * 以及基于 RAG（检索增强生成）的智能问答功能。
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    /** 文档服务，用于解析 PDF 文档 */
    private final DocumentService documentService;
    /** RAG 服务，用于将文档添加到向量数据库和执行智能问答 */
    private final RagService ragService;

    // 简单的文档记录存储 (实际项目应使用数据库)
    /** 文档记录映射表，使用 ConcurrentHashMap 保证线程安全，key 为文档 ID，value 为文档记录 */
    private static final Map<String, DocumentRecord> documentRecords = new ConcurrentHashMap<>();

    /**
     * 文档记录内部类
     * 用于存储上传文档的基本信息，包括文件名、大小、上传时间、分段数等。
     */
    @Data
    private static class DocumentRecord {
        /** 文档唯一标识 */
        private String id;
        /** 文件名称 */
        private String filename;
        /** 文件大小（字节） */
        private long size;
        /** 上传时间，格式为 yyyy-MM-dd HH:mm:ss */
        private String uploadTime;
        /** 文档分段数量，用于 RAG 检索 */
        private int segments;
        /** 文档状态 */
        private String status;

        /**
         * 构造文档记录
         *
         * @param id 文档唯一标识
         * @param filename 文件名称
         * @param size 文件大小（字节）
         */
        public DocumentRecord(String id, String filename, long size) {
            this.id = id;
            this.filename = filename;
            this.size = size;
            // 初始化上传时间为当前时间
            this.uploadTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            this.segments = 0;
            this.status = "已上传";
        }
    }

    /**
     * 管理后台首页
     * 访问 /admin 时重定向到文档上传页面
     *
     * @return 重定向到 /admin/upload
     */
    @GetMapping
    public String index() {
        return "redirect:/admin/upload";
    }

    /**
     * 文档上传页面
     * 提供单个/批量 PDF 文件上传的 Web 界面
     *
     * @return 文档上传页面的视图名称
     */
    @GetMapping("/upload")
    public String uploadPage() {
        return "admin/upload";
    }

    /**
     * 问答测试页面
     * 提供基于 RAG 的智能问答测试界面
     *
     * @return 问答测试页面的视图名称
     */
    @GetMapping("/chat")
    public String chatPage() {
        return "admin/chat";
    }

    /**
     * 文档管理页面
     * 展示所有已上传文档的列表及其状态信息
     *
     * @param model Spring MVC 数据模型，用于向视图传递文档列表数据
     * @return 文档管理页面的视图名称
     */
    @GetMapping("/documents")
    public String documentsPage(Model model) {
        // 将所有文档记录添加到模型中，供前端页面渲染使用
        model.addAttribute("documents", documentRecords.values());
        return "admin/documents";
    }

    /**
     * 上传 PDF 文档 (API)
     * 接收单个 PDF 文件，解析后添加到向量数据库中，支持后续的 RAG 检索问答
     *
     * @param file 上传的 PDF 文件，通过 multipart/form-data 方式提交
     * @return 包含处理结果的 ResponseEntity，成功时返回文档信息，失败时返回错误信息
     */
    @PostMapping("/api/upload")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            // 校验文件是否为空
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "文件不能为空"
                ));
            }

            // 校验文件类型，仅支持 PDF 格式
            if (!file.getOriginalFilename().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "只支持 PDF 文件"
                ));
            }

            // 使用 DocumentService 解析 PDF 文档内容
            var document = documentService.parsePdf(file);

            // 将解析后的文档添加到向量数据库中，用于后续的相似度检索
            ragService.addDocument(document);

            // 生成唯一 ID 并记录文档信息到内存存储中
            String docId = UUID.randomUUID().toString();
            DocumentRecord record = new DocumentRecord(
                    docId,
                    file.getOriginalFilename(),
                    file.getSize()
            );
            documentRecords.put(docId, record);

            // 返回成功响应，包含文档的详细信息
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "文档上传并处理成功",
                    "data", Map.of(
                            "id", docId,
                            "filename", file.getOriginalFilename(),
                            "size", formatFileSize(file.getSize())
                    )
            ));
        } catch (Exception e) {
            // 记录异常日志并返回错误响应
            log.error("上传 PDF 失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "上传失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 批量上传 PDF (API)
     * 接收多个 PDF 文件，逐个解析后添加到向量数据库中
     *
     * @param files 上传的 PDF 文件列表，通过 multipart/form-data 方式提交
     * @return 包含处理结果的 ResponseEntity，返回成功/失败数量及详细错误信息
     */
    @PostMapping("/api/upload/batch")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> uploadMultiplePdfs(
            @RequestParam("files") List<MultipartFile> files) {
        try {
            // 校验文件列表是否为空
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "文件列表不能为空"
                ));
            }

            int successCount = 0;  // 成功处理的文件数量
            int failCount = 0;     // 失败的文件数量
            List<String> errors = new ArrayList<>();  // 收集每个文件的错误信息

            // 遍历处理每个上传的文件
            for (MultipartFile file : files) {
                try {
                    // 解析 PDF 并添加到向量数据库
                    var document = documentService.parsePdf(file);
                    ragService.addDocument(document);

                    // 生成唯一 ID 并记录文档信息
                    String docId = UUID.randomUUID().toString();
                    DocumentRecord record = new DocumentRecord(
                            docId,
                            file.getOriginalFilename(),
                            file.getSize()
                    );
                    documentRecords.put(docId, record);
                    successCount++;  // 成功计数
                } catch (Exception e) {
                    // 单个文件处理失败不影响其他文件，记录错误信息
                    failCount++;
                    errors.add(file.getOriginalFilename() + ": " + e.getMessage());
                }
            }

            // 返回批量处理结果，包含成功/失败数量和错误详情
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", String.format("成功: %d, 失败: %d", successCount, failCount),
                    "data", Map.of(
                            "successCount", successCount,
                            "failCount", failCount,
                            "errors", errors
                    )
            ));
        } catch (Exception e) {
            // 全局异常处理
            log.error("批量上传 PDF 失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "批量上传失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 问答接口 (API)
     * 基于 RAG 技术实现智能问答，从已上传的文档中检索相关信息并生成回答
     *
     * @param request 请求体，包含 "question" 字段，表示用户提出的问题
     * @return 包含处理结果的 ResponseEntity，成功时返回答案，失败时返回错误信息
     */
    @PostMapping("/api/query")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> query(@RequestBody Map<String, String> request) {
        try {
            // 从请求体中获取用户问题
            String question = request.get("question");
            // 校验问题是否为空
            if (question == null || question.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "问题不能为空"
                ));
            }

            // 调用 RAG 服务进行智能问答
            String answer = ragService.query(question);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "answer", answer
            ));
        } catch (Exception e) {
            // 记录异常日志并返回错误响应
            log.error("问答失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "问答失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 流式问答接口 (SSE)
     * 基于 RAG 技术实现智能问答，使用 Server-Sent Events 实现流式输出
     * 前端可以实时看到 AI 生成的内容，实现打字机效果
     *
     * @param request 请求体，包含 "question" 字段，表示用户提出的问题
     * @return SseEmitter 用于推送流式数据
     */
    @PostMapping(value = "/api/query/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter queryStream(@RequestBody Map<String, String> request) {
        // 创建 SSE 发射器，设置超时时间为 1 分钟
        SseEmitter emitter = new SseEmitter(60 * 1000L);
        
        // 从请求体中获取用户问题
        String question = request.get("question");
        
        // 校验问题是否为空
        if (question == null || question.isEmpty()) {
            try {
                emitter.send(SseEmitter.event()
                        .name("error")
                        .data(Map.of("message", "问题不能为空")));
                emitter.complete();
            } catch (IOException e) {
                log.error("发送 SSE 消息失败", e);
            }
            return emitter;
        }

        // 构建完整回答
        StringBuilder fullAnswer = new StringBuilder();
        
        // 调用流式查询服务
        ragService.queryStream(
                question,
                // onNext: 每次生成新内容时
                (text) -> {
                    try {
                        fullAnswer.append(text);
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(Map.of(
                                        "content", text,
                                        "fullAnswer", fullAnswer.toString()
                                )));
                    } catch (IOException e) {
                        log.error("发送 SSE 消息失败", e);
                        emitter.completeWithError(e);
                    }
                },
                // onComplete: 完成时
                (tokenUsage) -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("done")
                                .data(Map.of(
                                        "fullAnswer", fullAnswer.toString(),
                                        "tokenUsage", Map.of(
                                                "promptTokens", tokenUsage.inputTokenCount(),
                                                "completionTokens", tokenUsage.outputTokenCount(),
                                                "totalTokens", tokenUsage.totalTokenCount()
                                        )
                                )));
                        emitter.complete();
                    } catch (IOException e) {
                        log.error("发送 SSE 完成消息失败", e);
                        emitter.completeWithError(e);
                    }
                },
                // onError: 错误时
                (error) -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(Map.of("message", "问答失败: " + error.getMessage())));
                        emitter.completeWithError(error);
                    } catch (IOException e) {
                        log.error("发送 SSE 错误消息失败", e);
                        emitter.completeWithError(e);
                    }
                }
        );

        return emitter;
    }

    /**
     * 获取文档列表 (API)
     * 返回所有已上传文档的记录列表
     *
     * @return 包含文档列表的 ResponseEntity
     */
    @GetMapping("/api/documents")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDocuments() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", documentRecords.values()  // 返回所有文档记录的集合
        ));
    }

    /**
     * 删除文档 (API)
     * 根据文档 ID 从内存记录中移除文档（注意：此操作不会从向量数据库中删除文档）
     *
     * @param id 要删除的文档唯一标识
     * @return 包含删除结果的 ResponseEntity
     */
    @DeleteMapping("/api/document/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteDocument(@PathVariable String id) {
        // 从内存记录映射表中移除指定文档
        documentRecords.remove(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "文档已删除"
        ));
    }

    /**
     * 格式化文件大小
     * 将字节数转换为人类可读的格式（B/KB/MB/GB）
     *
     * @param size 文件大小，单位为字节
     * @return 格式化后的文件大小字符串，如 "1.23 MB"
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            // 小于 1KB，直接显示为字节
            return size + " B";
        } else if (size < 1024 * 1024) {
            // 小于 1MB，显示为 KB
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            // 小于 1GB，显示为 MB
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            // 大于等于 1GB，显示为 GB
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
