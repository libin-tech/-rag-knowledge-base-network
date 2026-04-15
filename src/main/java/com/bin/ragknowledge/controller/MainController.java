package com.bin.ragknowledge.controller;

import com.bin.ragknowledge.service.DocumentService;
import com.bin.ragknowledge.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 主控制器
 * 提供核心的 RESTful API 接口，包括文档上传、批量上传、智能问答等功能。
 * 该控制器是系统的主要 API 入口，支持应用客户端的调用。
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MainController {

    /** 文档服务，用于解析 PDF 文档 */
    private final DocumentService documentService;
    /** RAG 服务，用于将文档添加到向量数据库和执行智能问答 */
    private final RagService ragService;

    /**
     * 上传 PDF 文档
     * 接收单个 PDF 文件，解析文档内容并添加到向量数据库中，支持后续的 RAG 检索问答
     *
     * @param file 上传的 PDF 文件，通过 multipart/form-data 方式提交
     * @return 包含处理结果的 ResponseEntity，成功时返回成功标识，失败时返回错误信息
     */
    @PostMapping("/upload")
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

            // 返回成功响应
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "文档上传并处理成功"
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
     * 批量上传 PDF 文档
     * 接收多个 PDF 文件，批量解析并添加到向量数据库中
     *
     * @param files 上传的 PDF 文件列表，通过 multipart/form-data 方式提交
     * @return 包含处理结果的 ResponseEntity，返回成功处理的文档数量
     */
    @PostMapping("/upload/batch")
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

            // 批量解析所有 PDF 文档
            var documents = documentService.parseMultiplePdfs(files);
            // 批量添加到向量数据库
            ragService.addDocuments(documents);

            // 返回成功响应，包含处理的文档数量
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "成功处理 " + documents.size() + " 个文档"
            ));
        } catch (Exception e) {
            // 记录异常日志并返回错误响应
            log.error("批量上传 PDF 失败", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "批量上传失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 问答接口
     * 基于 RAG 技术实现智能问答，从已上传的文档中检索相关信息并生成回答
     *
     * @param request 请求体，包含 "question" 字段，表示用户提出的问题
     * @return 包含处理结果的 ResponseEntity，成功时返回答案，失败时返回错误信息
     */
    @PostMapping("/query")
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
     * 健康检查
     * 提供服务运行状态检查接口，用于监控和负载均衡
     *
     * @return 包含服务状态信息的 ResponseEntity
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "message", "Family Assistant is running"
        ));
    }
}
