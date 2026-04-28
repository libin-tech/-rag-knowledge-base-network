-- =====================================================
-- LLM 和 Embedding 配置表 (llm_config)
-- RAG企业知识库问答系统 - 数据库建表脚本
-- =====================================================
-- 创建时间: 2026-04-28
-- 说明: 存储 LLM 和 Embedding 模型配置，支持后台管理和实时生效
-- =====================================================

-- ----------------------------
-- 1. 创建表结构
-- ----------------------------
CREATE TABLE IF NOT EXISTS llm_config (
    id              VARCHAR(64)          NOT NULL,
    config_type     VARCHAR(32)         NOT NULL,
    config_key     VARCHAR(64)          NOT NULL,
    config_value   TEXT,
    remark         VARCHAR(255),

    -- 审计字段
    create_time     TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creator         VARCHAR(64),
    modifier         VARCHAR(64),
    deleted         BOOLEAN             NOT NULL DEFAULT FALSE,
    version         INTEGER             NOT NULL DEFAULT 0,

    -- 主键约束
    CONSTRAINT pk_llm_config PRIMARY KEY (id),

    -- 唯一约束
    CONSTRAINT uk_config_type_key UNIQUE (config_type, config_key)
);

-- ----------------------------
-- 2. 创建索引
-- ----------------------------
CREATE INDEX idx_llm_config_type ON llm_config (config_type);
CREATE INDEX idx_llm_config_deleted ON llm_config (deleted);

-- ----------------------------
-- 3. 表和列注释
-- ----------------------------
COMMENT ON TABLE llm_config IS 'LLM和Embedding配置表，存储模型配置信息，支持后台管理和实时生效';

COMMENT ON COLUMN llm_config.id IS '配置唯一标识符(UUID)，主键';
COMMENT ON COLUMN llm_config.config_type IS '配置类型: LLM(大语言模型) 或 EMBEDDING(嵌入模型)';
COMMENT ON COLUMN llm_config.config_key IS '配置键: mode、apiKey、baseUrl、modelName、timeout 等';
COMMENT ON COLUMN llm_config.config_value IS '配置值';
COMMENT ON COLUMN llm_config.remark IS '备注说明';
COMMENT ON COLUMN llm_config.create_time IS '记录创建时间，由数据库自动维护';
COMMENT ON COLUMN llm_config.update_time IS '记录最后更新时间，由数据库自动维护';
COMMENT ON COLUMN llm_config.creator IS '记录创建者ID';
COMMENT ON COLUMN llm_config.modifier IS '记录最后修改者ID';
COMMENT ON COLUMN llm_config.deleted IS '逻辑删除标记，false-未删除，true-已删除';
COMMENT ON COLUMN llm_config.version IS '乐观锁版本号，用于并发控制';

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

CREATE TRIGGER tr_llm_config_update_time
    BEFORE UPDATE ON llm_config
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- 5. 初始化默认配置数据
-- ----------------------------
-- LLM 默认配置
INSERT INTO llm_config (id, config_type, config_key, config_value, remark) VALUES
    (gen_random_uuid(), 'LLM', 'mode', 'dashscope', 'LLM模式: dashscope/ollama/openai'),
    (gen_random_uuid(), 'LLM', 'dashscope_apiKey', '', '阿里云DashScope API Key'),
    (gen_random_uuid(), 'LLM', 'dashscope_modelName', 'qwen-plus', '阿里云DashScope 模型名称'),
    (gen_random_uuid(), 'LLM', 'ollama_baseUrl', 'http://127.0.0.1:11434', 'Ollama 服务地址'),
    (gen_random_uuid(), 'LLM', 'ollama_modelName', 'qwen3', 'Ollama 模型名称'),
    (gen_random_uuid(), 'LLM', 'ollama_timeout', '60s', 'Ollama 超时时间'),
    (gen_random_uuid(), 'LLM', 'openai_apiKey', '', 'OpenAI API Key'),
    (gen_random_uuid(), 'LLM', 'openai_baseUrl', 'https://api.openai.com', 'OpenAI 服务地址'),
    (gen_random_uuid(), 'LLM', 'openai_modelName', 'gpt-4o-mini', 'OpenAI 模型名称'),
    (gen_random_uuid(), 'LLM', 'openai_timeout', '120s', 'OpenAI 超时时间')
ON CONFLICT DO NOTHING;

-- Embedding 默认配置
INSERT INTO llm_config (id, config_type, config_key, config_value, remark) VALUES
    (gen_random_uuid(), 'EMBEDDING', 'mode', 'dashscope', 'Embedding模式: dashscope/ollama/openai'),
    (gen_random_uuid(), 'EMBEDDING', 'dashscope_apiKey', '', '阿里云DashScope API Key'),
    (gen_random_uuid(), 'EMBEDDING', 'dashscope_modelName', 'text-embedding-v3', '阿里云DashScope 模型名称'),
    (gen_random_uuid(), 'EMBEDDING', 'ollama_baseUrl', 'http://127.0.0.1:11434', 'Ollama 服务地址'),
    (gen_random_uuid(), 'EMBEDDING', 'ollama_modelName', 'nomic-embed-text', 'Ollama 模型名称'),
    (gen_random_uuid(), 'EMBEDDING', 'ollama_timeout', '60s', 'Ollama 超时时间'),
    (gen_random_uuid(), 'EMBEDDING', 'openai_apiKey', '', 'OpenAI API Key'),
    (gen_random_uuid(), 'EMBEDDING', 'openai_baseUrl', 'https://api.openai.com', 'OpenAI 服务地址'),
    (gen_random_uuid(), 'EMBEDDING', 'openai_modelName', 'text-embedding-3-small', 'OpenAI 模型名称'),
    (gen_random_uuid(), 'EMBEDDING', 'openai_timeout', '120s', 'OpenAI 超时时间')
ON CONFLICT DO NOTHING;