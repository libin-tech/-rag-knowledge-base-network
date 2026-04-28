-- =====================================================
-- LLM/Embedding 配置启用功能 (llm_config)
-- RAG企业知识库问答系统 - 数据库脚本
-- =====================================================
-- 创建时间: 2026-04-28
-- 说明: 为 LLM 和 Embedding 配置增加启用/停用功能，只能启用一个模型
-- =====================================================

-- ----------------------------
-- 1. 添加 enabled 字段
-- ----------------------------
ALTER TABLE llm_config ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN llm_config.enabled IS '启用状态，true-启用，false-停用，同类型只能启用一个';

-- ----------------------------
-- 2. 创建唯一约束：同类型下只能有一个启用的配置
-- ----------------------------
-- 注意：此约束需要应用层保证，检查时机在更新之后

-- ----------------------------
-- 3. 初始化现有配置的启用状态
-- ----------------------------
-- LLM mode 默认启用
UPDATE llm_config
SET enabled = TRUE
WHERE config_type = 'LLM' AND config_key = 'mode' AND deleted = false;

-- Embedding mode 默认启用
UPDATE llm_config
SET enabled = TRUE
WHERE config_type = 'EMBEDDING' AND config_key = 'mode' AND deleted = false;