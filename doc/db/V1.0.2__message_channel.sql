-- =====================================================
-- 消息渠道配置表 (message_channel)
-- RAG企业知识库问答系统 - 数据库建表脚本
-- =====================================================
-- 创建时间: 2026-04-28
-- 说明: 存储消息渠道配置，支持飞书、钉钉、企业微信等
-- =====================================================

-- ----------------------------
-- 1. 创建表结构
-- ----------------------------
CREATE TABLE IF NOT EXISTS message_channel (
    id              VARCHAR(64)          NOT NULL,
    channel_type    VARCHAR(32)         NOT NULL,
    channel_name    VARCHAR(64)         NOT NULL,
    enabled         BOOLEAN             NOT NULL DEFAULT true,
    config_json     TEXT,
    remark         VARCHAR(255),

    -- 审计字段
    create_time     TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creator         VARCHAR(64),
    modifier        VARCHAR(64),
    deleted         BOOLEAN             NOT NULL DEFAULT FALSE,
    version         INTEGER             NOT NULL DEFAULT 0,

    -- 主键约束
    CONSTRAINT pk_message_channel PRIMARY KEY (id),

    -- 唯一约束
    CONSTRAINT uk_channel_type UNIQUE (channel_type)
);

-- ----------------------------
-- 2. 创建索引
-- ----------------------------
CREATE INDEX idx_message_channel_type ON message_channel (channel_type);
CREATE INDEX idx_message_channel_enabled ON message_channel (enabled);
CREATE INDEX idx_message_channel_deleted ON message_channel (deleted);

-- ----------------------------
-- 3. 表和列注释
-- ----------------------------
COMMENT ON TABLE message_channel IS '消息渠道配置表，存储各平台机器人配置';

COMMENT ON COLUMN message_channel.id IS '渠道唯一标识符(UUID)，主键';
COMMENT ON COLUMN message_channel.channel_type IS '渠道类型: FEISHU(飞书)、DINGTALK(钉钉)、WECHAT_WORK(企业微信)';
COMMENT ON COLUMN message_channel.channel_name IS '渠道名称';
COMMENT ON COLUMN message_channel.enabled IS '是否启用';
COMMENT ON COLUMN message_channel.config_json IS '渠道配置JSON';
COMMENT ON COLUMN message_channel.remark IS '备注说明';
COMMENT ON COLUMN message_channel.create_time IS '记录创建时间';
COMMENT ON COLUMN message_channel.update_time IS '记录最后更新时间';
COMMENT ON COLUMN message_channel.creator IS '记录创建者ID';
COMMENT ON COLUMN message_channel.modifier IS '记录最后修改者ID';
COMMENT ON COLUMN message_channel.deleted IS '逻辑删除标记';
COMMENT ON COLUMN message_channel.version IS '乐观锁版本号';

-- ----------------------------
-- 4. 创建更新触发器
-- ----------------------------
CREATE TRIGGER tr_message_channel_update_time
    BEFORE UPDATE ON message_channel
    FOR EACH ROW
    EXECUTE FUNCTION update_modified_column();

-- ----------------------------
-- 5. 初始化默认渠道配置
-- ----------------------------
-- 飞书渠道
INSERT INTO message_channel (id, channel_type, channel_name, enabled, config_json, remark) VALUES
    (gen_random_uuid(), 'FEISHU', '飞书机器人', false, '{"appId":"","appSecret":"","encryptKey":"","verificationToken":""}', '飞书开放平台应用配置')
ON CONFLICT DO NOTHING;

-- 钉钉渠道
INSERT INTO message_channel (id, channel_type, channel_name, enabled, config_json, remark) VALUES
    (gen_random_uuid(), 'DINGTALK', '钉钉机器人', false, '{"appKey":"","appSecret":"","agentId":"","corpId":""}', '钉钉企业应用配置')
ON CONFLICT DO NOTHING;

-- 企业微信渠道 (预留)
INSERT INTO message_channel (id, channel_type, channel_name, enabled, config_json, remark) VALUES
    (gen_random_uuid(), 'WECHAT_WORK', '企业微信', false, '{"corpId":"","corpSecret":"","agentId":"","token":"","encodingAesKey":""}', '企业微信配置 - 预留')
ON CONFLICT DO NOTHING;