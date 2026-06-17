package com.bintech.rag.controller;

import cn.hutool.json.JSONUtil;
import com.bintech.rag.config.AuthProperties;
import com.bintech.rag.repository.entity.DocumentMetadataEntity;
import com.bintech.rag.service.DocumentMetadataService;
import com.bintech.rag.service.DocumentService;
import com.bintech.rag.service.ObjectStorageService;
import com.bintech.rag.service.RagService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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


@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DocumentService documentService;
    private final RagService ragService;
    private final ObjectStorageService objectStorageService;
    private final DocumentMetadataService documentMetadataService;
    private final AuthProperties authProperties;
    private final ObjectMapper objectMapper;

    @PostMapping("/api/upload")
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

    @PostMapping("/api/upload/batch")
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

    @PostMapping("/api/query")
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

    @PostMapping(value = "/api/query/stream")
    public SseEmitter queryStream(@RequestBody Map<String, String> request) {

        String question = request.get("question");
        String knowledgeBaseId = request.getOrDefault("knowledgeBaseId", "default");

        SseEmitter emitter = new SseEmitter(60_000L);
        StringBuilder fullAnswer = new StringBuilder();

        emitter.onCompletion(() -> log.debug("SSE 连接完成"));
        emitter.onTimeout(() -> {
            log.warn("SSE 连接超时");
            emitter.complete();
        });
        emitter.onError(ex -> log.error("SSE 连接出错", ex));

        if (question == null || question.isEmpty()) {
            try {
                emitter.send(SseEmitter.event().name("error")
                        .data(JSONUtil.toJsonStr(Map.of("message", "问题不能为空"))));
            } catch (IOException e) {
                log.error("发送 SSE 错误消息失败", e);
            }
            emitter.complete();
            return emitter;
        }

        try {
            emitter.send(SseEmitter.event().name("thinking")
                    .data(JSONUtil.toJsonStr(Map.of("thinking", "正在检索知识库并分析问题..."))));
        } catch (IOException e) {
            log.error("发送 thinking 事件失败", e);
            emitter.completeWithError(e);
            return emitter;
        }

        ragService.queryStream(
                question,
                knowledgeBaseId,
                text -> {
                    try {
                        fullAnswer.append(text);
                        emitter.send(SseEmitter.event().name("message").data(JSONUtil.toJsonStr(Map.of(
                                "content", text,
                                "fullAnswer", fullAnswer.toString()
                        ))));
                    } catch (IOException e) {
                        log.error("发送 SSE 消息失败", e);
                    }
                },
                tokenUsage -> {
                    try {
                        emitter.send(SseEmitter.event().name("done").data(JSONUtil.toJsonStr(Map.of(
                                "fullAnswer", fullAnswer.toString(),
                                "tokenUsage", Map.of(
                                        "promptTokens", tokenUsage.inputTokenCount(),
                                        "completionTokens", tokenUsage.outputTokenCount(),
                                        "totalTokens", tokenUsage.totalTokenCount()
                                )
                        ))));
                    } catch (IOException e) {
                        log.error("发送 SSE 完成消息失败", e);
                    } finally {
                        emitter.complete();
                    }
                },
                error -> {
                    try {
                        emitter.send(SseEmitter.event().name("error")
                                .data(JSONUtil.toJsonStr(Map.of("message", "问答失败: " + error.getMessage()))));
                    } catch (IOException e) {
                        log.error("发送 SSE 错误消息失败", e);
                    } finally {
                        emitter.completeWithError(error);
                    }
                }
        );

        return emitter;
    }

    @GetMapping("/api/documents")
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

    @DeleteMapping("/api/document/{id}")
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

        String documentId = java.util.UUID.randomUUID().toString();
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

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
