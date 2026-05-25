# 变更记录

本文档记录系统的主要变更内容。

## v1.2.2 - 2026-05-25

### 项目结构重构
- **前后端分离目录**：后端代码迁移至 `backend/` 目录，前端代码保留在 `frontend/` 目录
- **规范文件拆分**：CLAUDE.md 按前后端拆分为 `backend/CLAUDE.md` 和 `frontend/CLAUDE.md`
- **AGENTS.md 精简**：AGENTS.md 仅保留项目通用规范（技术栈、目录结构、通用代码质量、分支规范）
- **文档更新**：README.md / README_en.md 项目结构图及命令路径同步更新

### 配置变更
- `pom.xml`、`Dockerfile` 移至 `backend/` 目录
- `docker-compose.yml` build context 更新为 `./backend`
- `frontend/vite.config.js` 构建输出路径更新为 `../backend/src/main/resources/static`

## v1.2.1 - 2026-05-21

### 优化调整
- **SSE 重构**：`queryStream` 接口使用 Spring `SseEmitter` 替代直接操作 `HttpServletResponse` 输出流
- **Sa-Token 异步分发修复**：`SaTokenConfigure` 拦截器增加 `DispatcherType.ASYNC` 放行，避免 SseEmitter 异步线程鉴权失败
- **前端适配**：移除 Chat.vue 中的 `AbortController` 和超时逻辑，由 SseEmitter 原生管理连接生命周期
- **Maven 坐标调整**：`groupId` 改为 `com.bintech`，`artifactId` 改为 `rag`

## v1.2.0 - 2026-05-21

### 重大变更
- **认证框架替换**：移除 Spring Security，替换为 Sa-Token 1.36.0 认证方式
- **前端架构重构**：移除 Thymeleaf + Bootstrap，替换为 Vue 3 + Ant Design Vue 4 单页应用

### 新增功能
- Sa-Token 令牌认证（Authorization 请求头传递）
- 令牌登录/登出接口（`/api/auth/login`、`/api/auth/logout`、`/api/auth/info`）
- Vue 3 前端项目（`frontend/` 目录），支持独立开发模式
- 前端登录页（Ant Design Vue 组件）
- 前端路由守卫（未登录自动跳转登录页）
- Axios 拦截器自动携带令牌和处理 401 响应
- 支持跨域代理开发（Vite dev server → Spring Boot backend）
- SPA 路由转发（`/login` 和 `/admin/**` 自动回退至 `index.html`）
- 未登录异常全局处理器（返回 401 JSON 响应）
- 新增 `SaTokenConfigure.java`、`SpaController.java`

### 移除功能
- 移除 Spring Security 依赖（`spring-boot-starter-security`）
- 移除 Thymeleaf 模板及 Spring Security 集成
- 删除 `SecurityConfig.java`、`PasswordEncoderConfig.java`、`CustomUserDetailsService.java`
- 删除服务端页面路由（页面渲染由 Vue Router 接管）
- 删除所有 Thymeleaf 模板视图方法

### 配置变更
- 新增 `sa-token` 配置段（`application.yml`）
- 新增 `frontend/` 前端项目及 `package.json`、`vite.config.js`
- 前端构建产物输出至 `src/main/resources/static/`

### 部署变更
- 新增前端构建步骤：`cd frontend && npm install && npm run build`
- 开发模式支持前后端分离启动
- Docker Compose 部署顺序调整：先构建前端再打包后端

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
