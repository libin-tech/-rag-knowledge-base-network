package com.bintech.rag.service;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 文档解析服务
 * <p>
 * 职责：负责处理各种格式文档的解析工作，当前主要支持 PDF 文档的解析。
 * 该服务是 RAG（检索增强生成）系统的前置组件，将原始文档转换为
 * LangChain4j 的 Document 对象，以便后续进行文档分割和向量化处理。
 * </p>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li>接收上传的文件（MultipartFile）</li>
 *   <li>使用 Apache PDFBox 解析器读取 PDF 内容</li>
 *   <li>提取文档元数据并记录日志</li>
 *   <li>返回标准化的 Document 对象供下游服务使用</li>
 * </ol>
 */
@Slf4j
@Service
public class DocumentService {

    /**
     * 解析单个 PDF 文件
     * <p>
     * 工作流程：
     * 1. 从 MultipartFile 获取输入流
     * 2. 使用 ApachePdfBoxDocumentParser 解析 PDF 内容
     * 3. 记录解析成功日志，包含文件名和元数据信息
     * 4. 返回解析后的 Document 对象
     * </p>
     *
     * @param file 上传的 PDF 文件，类型为 Spring 的 MultipartFile
     * @return 解析后的 LangChain4j Document 对象，包含文档内容和元数据
     * @throws IOException 当文件读取或解析过程中发生错误时抛出
     */
    public Document parsePdf(MultipartFile file) throws IOException {
        // 使用 try-with-resources 确保输入流在使用完毕后自动关闭，避免资源泄漏
        try (InputStream inputStream = file.getInputStream()) {
            // 创建 PDF 文档解析器实例
            ApachePdfBoxDocumentParser parser = new ApachePdfBoxDocumentParser();
            // 执行解析操作，将输入流转换为 Document 对象
            Document document = parser.parse(inputStream);
            // 记录成功日志，输出原始文件名和文档元数据（如页数等信息）
            log.info("成功解析 PDF 文档: {}, 页数: {}",
                    file.getOriginalFilename(),
                    document.metadata().toMap());
            return document;
        }
    }

    /**
     * 批量解析多个 PDF 文件
     * <p>
     * 工作流程：
     * 1. 遍历传入的文件列表
     * 2. 逐个调用 parsePdf 方法进行解析
     * 3. 解析成功的文件添加到结果列表中
     * 4. 解析失败的文件记录错误日志后跳过，不影响其他文件的解析
     * 5. 返回所有成功解析的 Document 对象列表
     * </p>
     * <p>
     * 容错策略：采用"尽力而为"的策略，单个文件解析失败不会中断整个流程，
     * 而是记录错误后继续处理下一个文件，确保批量处理的鲁棒性。
     * </p>
     *
     * @param files 待解析的 PDF 文件列表，每个元素为 MultipartFile 类型
     * @return 成功解析的 Document 对象列表，失败的文件不会包含在内
     * @throws IOException 当发生非预期的 IO 异常时抛出（正常情况下单个文件失败会被捕获）
     */
    public List<Document> parseMultiplePdfs(List<MultipartFile> files) throws IOException {
        // 初始化结果列表，用于存放成功解析的文档
        List<Document> documents = new ArrayList<>();
        // 遍历所有待解析的文件
        for (MultipartFile file : files) {
            try {
                // 调用单文件解析方法
                Document document = parsePdf(file);
                // 将解析成功的文档添加到结果列表
                documents.add(document);
            } catch (Exception e) {
                // 捕获异常并记录错误日志，包含文件名和异常堆栈，便于问题排查
                // 注意：此处不抛出异常，确保单个文件失败不影响整体批量处理流程
                log.error("解析 PDF 失败: {}", file.getOriginalFilename(), e);
            }
        }
        return documents;
    }
}
