package com.bintech.rag.repository.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 文档元数据实体类
 * <p>
 * 存储已上传文档的元信息，包括文件名、文件大小、存储路径、向量ID等。
 * 该实体与 document_metadata 表一一对应。
 * </p>
 *
 * @author Family Assistant Team
 * @version 1.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("document_metadata")
public class DocumentMetadataEntity extends BaseEntity {

    /**
     * 文件名
     * <p>
     * 原始上传文件的名称，包含文件扩展名。
     * 例如：技术文档.pdf
     * </p>
     */
    @TableField("filename")
    private String filename;

    /**
     * 内容类型
     * <p>
     * 文件的MIME类型，用于标识文件格式。
     * 例如：application/pdf
     * </p>
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 文件大小
     * <p>
     * 文件的字节大小，用于前端展示和统计。
     * </p>
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 对象存储键
     * <p>
     * 对象存储服务（如MinIO）中的文件路径，唯一标识。
     * 格式：documents/{documentId}/{filename}
     * </p>
     */
    @TableField("object_key")
    private String objectKey;

    /**
     * 向量文档ID
     * <p>
     * 文档在向量数据库（如Milvus）中的文档ID，
     * 用于关联文档与其向量化后的数据。
     * </p>
     */
    @TableField("vector_doc_id")
    private String vectorDocId;

    /**
     * 分段数量
     * <p>
     * 文档被分割成的文本块（chunk）数量，
     * 用于了解文档的处理粒度。
     * </p>
     */
    @TableField("segment_count")
    private Integer segmentCount;

    /**
     * 向量ID列表
     * <p>
     * 文档各分段在向量数据库中的ID列表，JSON格式存储。
     * 用于删除文档时清理向量数据。
     * </p>
     */
    @TableField("vector_ids")
    private String vectorIds;

    /**
     * 上传时间
     * <p>
     * 文档上传到系统的时间，用于排序和展示。
     * </p>
     */
    @TableField("upload_time")
    private LocalDateTime uploadTime;

    /**
     * 知识库ID
     * <p>
     * 文档所属的知识库标识，用于按知识库进行数据隔离。
     * </p>
     */
    @TableField("knowledge_base_id")
    private String knowledgeBaseId;
}