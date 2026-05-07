package com.bin.ragknowledge.controller;

import com.bin.ragknowledge.config.AuthProperties;
import com.bin.ragknowledge.repository.entity.DocumentMetadataEntity;
import com.bin.ragknowledge.service.DocumentMetadataService;
import com.bin.ragknowledge.service.DocumentService;
import com.bin.ragknowledge.service.ObjectStorageService;
import com.bin.ragknowledge.service.RagService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private final RagService ragService;

    private final ObjectStorageService objectStorageService;

    private final DocumentMetadataService documentMetadataService;

    private final AuthProperties authProperties;

    private final ObjectMapper objectMapper;

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
        return "admin/documents";
    }

    /**
     * 模型配置页面
     * 用于配置 LLM 和 Embedding 模型参数
     *
     * @return 模型配置页面的视图名称
     */
    @GetMapping("/config")
    public String configPage() {
        return "admin/config";
    }

    /**
     * 消息渠道管理页面
     * 用于配置飞书、钉钉等消息渠道
     *
     * @return 消息渠道管理页面的视图名称
     */
    @GetMapping("/channel")
    public String channelPage() {
        return "admin/channel";
    }

    /**
     * 知识库管理页面
     * 用于管理知识库的增删改查
     *
     * @return 知识库管理页面的视图名称
     */
    @GetMapping("/knowledge-base")
    public String knowledgeBasePage() {
        return "admin/knowledge-base";
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
    public ResponseEntity<Map<String, Object>> uploadPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "knowledgeBaseId", defaultValue = "default") String knowledgeBaseId) {
        try {
            return handleSingleUpload(file, knowledgeBaseId);
        } catch (Exception e) {
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
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "knowledgeBaseId", defaultValue = "default") String knowledgeBaseId) {
        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "文件列表不能为空"
                ));
            }

            int successCount = 0;
            int failCount = 0;
            List<String> errors = new ArrayList<>();
            for (MultipartFile file : files) {
                try {
                    ResponseEntity<Map<String, Object>> result = handleSingleUpload(file, knowledgeBaseId);
                    if (result.getStatusCode().is2xxSuccessful()) {
                        successCount++;
                    } else {
                        failCount++;
                        errors.add(file.getOriginalFilename() + ": 上传失败");
                    }
                } catch (Exception e) {
                    failCount++;
                    errors.add(file.getOriginalFilename() + ": " + e.getMessage());
                }
            }

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
            String question = request.get("question");
            String knowledgeBaseId = request.getOrDefault("knowledgeBaseId", "default");
            if (question == null || question.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "问题不能为空"
                ));
            }

            String answer = ragService.query(question, knowledgeBaseId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "answer", answer
            ));
        } catch (Exception e) {
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
        SseEmitter emitter = new SseEmitter(60 * 1000L);
        String question = request.get("question");
        String knowledgeBaseId = request.getOrDefault("knowledgeBaseId", "default");

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

        StringBuilder fullAnswer = new StringBuilder();

        ragService.queryStream(
                question,
                knowledgeBaseId,
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
    public ResponseEntity<Map<String, Object>> getDocuments(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String knowledgeBaseId) {
        int currentPage = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<DocumentMetadataEntity> resultPage;
        if (knowledgeBaseId != null && !knowledgeBaseId.isEmpty()) {
            resultPage = documentMetadataService.pageByKnowledgeBaseId(currentPage, pageSize, knowledgeBaseId);
        } else {
            resultPage = documentMetadataService.page(currentPage, pageSize);
        }
        List<Map<String, Object>> records = resultPage.getRecords().stream()
                .map(this::toDocumentResponse)
                .toList();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", records,
                "pagination", Map.of(
                        "page", currentPage,
                        "size", pageSize,
                        "totalPages", resultPage.getPages(),
                        "totalElements", resultPage.getTotal()
                )
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
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<Map<String, Object>> deleteDocument(
            @PathVariable String id,
            @RequestBody DeleteDocumentRequest request) {
        if (request == null || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "请输入删除确认密码"
            ));
        }
        if (!authProperties.getAdminPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "success", false,
                    "message", "删除密码校验失败"
            ));
        }
        DocumentMetadataEntity entity = documentMetadataService.getById(id);
        if (entity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "文档不存在"
            ));
        }
        try {
            List<String> vectorIds = objectMapper.readValue(entity.getVectorIds(), new TypeReference<>() {
            });
            ragService.deleteDocumentVectors(vectorIds);
            objectStorageService.deleteFile(entity.getObjectKey());
            documentMetadataService.deleteById(id);
        } catch (Exception e) {
            log.error("删除文档失败, id: {}", id, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "删除失败: " + e.getMessage()
            ));
        }
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "文档已删除（pgsql、minio、向量库）"
        ));
    }

    @GetMapping(value = "/api/document/{id}/preview", produces = MediaType.APPLICATION_PDF_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> previewDocument(@PathVariable String id) {
        DocumentMetadataEntity entity = documentMetadataService.getById(id);
        if (entity == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try (InputStream inputStream = objectStorageService.getFileStream(entity.getObjectKey())) {
            byte[] content = inputStream.readAllBytes();
            String contentType = objectStorageService.getContentType(entity.getObjectKey());
            String encodedFilename = java.net.URLEncoder.encode(entity.getFilename(), StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType == null ? MediaType.APPLICATION_PDF_VALUE : contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedFilename)
                    .body(content);
        } catch (Exception e) {
            log.error("预览文档失败, id: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private ResponseEntity<Map<String, Object>> handleSingleUpload(MultipartFile file, String knowledgeBaseId) throws Exception {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "文件不能为空"
            ));
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "只支持 PDF 文件"
            ));
        }
        var document = documentService.parsePdf(file);
        RagService.DocumentVectorResult vectorResult = ragService.addDocument(document, knowledgeBaseId);

        String documentId = UUID.randomUUID().toString();
        String objectKey = "documents/" + documentId + "/" + filename;
        objectStorageService.uploadFile(objectKey, file);

        DocumentMetadataEntity entity = new DocumentMetadataEntity();
        entity.setId(documentId);
        entity.setFilename(filename);
        entity.setFileSize(file.getSize());
        entity.setContentType(file.getContentType() == null ? MediaType.APPLICATION_PDF_VALUE : file.getContentType());
        entity.setObjectKey(objectKey);
        entity.setVectorDocId(vectorResult.vectorDocId());
        entity.setSegmentCount(vectorResult.segmentCount());
        entity.setVectorIds(objectMapper.writeValueAsString(vectorResult.vectorIds()));
        entity.setUploadTime(LocalDateTime.now());
        entity.setKnowledgeBaseId(knowledgeBaseId);
        documentMetadataService.save(entity);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "文档上传并处理成功",
                "data", Map.of(
                        "id", documentId,
                        "filename", filename,
                        "size", formatFileSize(file.getSize())
                )
        ));
    }

    private Map<String, Object> toDocumentResponse(DocumentMetadataEntity entity) {
        return Map.of(
                "id", entity.getId(),
                "filename", entity.getFilename(),
                "size", formatFileSize(entity.getFileSize()),
                "uploadTime", entity.getUploadTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                "status", "已上传",
                "segmentCount", entity.getSegmentCount()
        );
    }

    @lombok.Data
    private static class DeleteDocumentRequest {
        private String password;
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
