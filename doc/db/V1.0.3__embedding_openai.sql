-- =====================================================
-- Embedding OpenAI 配置扩展 (llm_config)
-- RAG企业知识库问答系统 - 数据库脚本
-- =====================================================
-- 创建时间: 2026-04-28
-- 说明: 为 Embedding 模型添加 OpenAI 支持，新增4个配置项
-- =====================================================

-- ----------------------------
-- 1. 初始化 Embedding OpenAI 配置
-- ----------------------------
INSERT INTO llm_config (id, config_type, config_key, config_value, remark, creator, modifier)
SELECT gen_random_uuid(), 'EMBEDDING', 'openai_apiKey', '', 'OpenAI API Key', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM llm_config WHERE config_type = 'EMBEDDING' AND config_key = 'openai_apiKey' AND deleted = false
);

INSERT INTO llm_config (id, config_type, config_key, config_value, remark, creator, modifier)
SELECT gen_random_uuid(), 'EMBEDDING', 'openai_baseUrl', 'https://api.openai.com', 'OpenAI 服务地址', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM llm_config WHERE config_type = 'EMBEDDING' AND config_key = 'openai_baseUrl' AND deleted = false
);

INSERT INTO llm_config (id, config_type, config_key, config_value, remark, creator, modifier)
SELECT gen_random_uuid(), 'EMBEDDING', 'openai_modelName', 'text-embedding-3-small', 'OpenAI 模型名称', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM llm_config WHERE config_type = 'EMBEDDING' AND config_key = 'openai_modelName' AND deleted = false
);

INSERT INTO llm_config (id, config_type, config_key, config_value, remark, creator, modifier)
SELECT gen_random_uuid(), 'EMBEDDING', 'openai_timeout', '120s', 'OpenAI 超时时间', 'system', 'system'
WHERE NOT EXISTS (
    SELECT 1 FROM llm_config WHERE config_type = 'EMBEDDING' AND config_key = 'openai_timeout' AND deleted = false
);

-- ----------------------------
-- 2. 更新 Embedding mode 配置的备注
-- ----------------------------
UPDATE llm_config
SET remark = 'Embedding模式: dashscope/ollama/openai', modifier = 'system'
WHERE config_type = 'EMBEDDING' AND config_key = 'mode' AND deleted = false;