-- =====================================================
-- 文档元数据表 (document_metadata)
-- RAG企业知识库问答系统 - 数据库建表脚本
-- =====================================================
-- 创建时间: 2026-04-24
-- 说明: 存储已上传文档的元信息，用于文档管理和RAG检索
-- =====================================================

-- ----------------------------
-- 1. 创建表结构
-- ----------------------------
CREATE TABLE IF NOT EXISTS document_metadata (
    id              VARCHAR(64)          NOT NULL,
    filename        VARCHAR(255)        NOT NULL,
    content_type    VARCHAR(100)        NOT NULL,
    file_size       BIGINT              NOT NULL,
    object_key      VARCHAR(255)        NOT NULL,
    vector_doc_id   VARCHAR(64)         NOT NULL,
    segment_count   INTEGER             NOT NULL DEFAULT 0,
    vector_ids      TEXT                NOT NULL,
    upload_time     TIMESTAMP           NOT NULL,

    -- 审计字段
    create_time     TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creator         VARCHAR(64),
    modifier        VARCHAR(64),
    deleted         BOOLEAN             NOT NULL DEFAULT FALSE,
    version         INTEGER             NOT NULL DEFAULT 0,

    -- 主键约束
    CONSTRAINT pk_document_metadata PRIMARY KEY (id),

    -- 唯一约束
    CONSTRAINT uk_object_key UNIQUE (object_key)
);

-- ----------------------------
-- 2. 创建索引
-- ----------------------------
CREATE INDEX idx_upload_time ON document_metadata (upload_time DESC);
CREATE INDEX idx_vector_doc_id ON document_metadata (vector_doc_id);
CREATE INDEX idx_deleted ON document_metadata (deleted);

-- ----------------------------
-- 3. 表和列注释
-- ----------------------------
COMMENT ON TABLE document_metadata IS '文档元数据表，存储已上传文档的基本信息和向量关联数据';

COMMENT ON COLUMN document_metadata.id IS '文档唯一标识符(UUID)，主键';
COMMENT ON COLUMN document_metadata.filename IS '文件名，包含文件扩展名，如：技术文档.pdf';
COMMENT ON COLUMN document_metadata.content_type IS '文件的MIME类型，如：application/pdf';
COMMENT ON COLUMN document_metadata.file_size IS '文件大小，单位为字节';
COMMENT ON COLUMN document_metadata.object_key IS '对象存储键，格式为：documents/{uuid}/{filename}';
COMMENT ON COLUMN document_metadata.vector_doc_id IS '文档在向量数据库（如Milvus）中的文档ID';
COMMENT ON COLUMN document_metadata.segment_count IS '文档被分割成的文本块（chunk）数量';
COMMENT ON COLUMN document_metadata.vector_ids IS '各分块在向量数据库中的ID列表，JSON数组格式';
COMMENT ON COLUMN document_metadata.upload_time IS '文件上传到系统的时间';
COMMENT ON COLUMN document_metadata.create_time IS '记录创建时间，由数据库自动维护';
COMMENT ON COLUMN document_metadata.update_time IS '记录最后更新时间，由数据库自动维护';
COMMENT ON COLUMN document_metadata.creator IS '记录创建者ID';
COMMENT ON COLUMN document_metadata.modifier IS '记录最后修改者ID';
COMMENT ON COLUMN document_metadata.deleted IS '逻辑删除标记，false-未删除，true-已删除';
COMMENT ON COLUMN document_metadata.version IS '乐观锁版本号，用于并发控制';

-- ----------------------------
-- 4. 创建更新触发器
-- ----------------------------
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_document_metadata_update_time
    BEFORE UPDATE ON document_metadata
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();