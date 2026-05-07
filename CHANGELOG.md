# 变更记录

本文档记录系统的主要变更内容。

## v1.1.0 - 2026-05-07

### 新增功能
- 新增知识库管理功能（创建、启用/停用、切换）
- 新增知识库管理页面 `/admin/knowledge-base`
- 新增知识库相关接口（列表、创建、更新、删除）
- 新增 `KnowledgeBaseEntity`、`KnowledgeBaseMapper`、`KnowledgeBaseService`、`KnowledgeBaseController`
- 数据库初始化脚本整合至 `doc/db/init.sql`
- 新增 VSCode 调试配置（`.vscode/launch.json`、`.vscode/settings.json`）

### 优化调整
- 完善项目结构，新增 `filter`、`interceptor`、`context` 包
- 优化配置类结构
- 优化实体类继承结构
- 删除 `TraceIdFilter`，使用其他方式实现链路追踪

### 数据库变更
- 合并所有版本迁移脚本至 `init.sql`
- 新增 `knowledge_base` 表及索引
- 预置默认知识库数据
