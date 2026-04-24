# RAG 知识库系统

[English](README_en.md) | 中文

基于 LangChain4j 的 RAG（Retrieval-Augmented Generation，检索增强生成）知识库问答系统，支持 Web 管理后台、REST API、流式问答，并集成飞书与钉钉机器人。

## 功能特性

- RAG 全流程：PDF 解析、分块、向量化、Milvus 检索、LLM 生成
- 多模型支持：DashScope（云端）与 Ollama（本地）可切换
- 双入口调用：管理后台页面 + 开放 REST API
- 流式问答：支持 SSE 流式输出
- 机器人接入：飞书机器人、钉钉机器人
- 可观测性：关键链路日志（含检索增强阶段耗时）


## 技术栈

- Java 21
- Spring Boot 4.0.5
- LangChain4j 0.36.2
- Milvus 2.4.x
- Hutool 5.8.x
- Docker / Docker Compose

## 项目结构

- `src/main/java/com/bin/ragknowledge/controller`：页面与 API 控制器
- `src/main/java/com/bin/ragknowledge/service`：文档解析、RAG 检索问答核心逻辑
- `src/main/resources/application.yml`：系统配置（模型、向量库、机器人等）
- `docker-compose.yml`：Milvus + MinIO + Etcd + 应用一体化编排

## 快速开始

### 方式一：Docker Compose（推荐）

1. 准备 `.env` 文件（可参考项目中的 `.env.example`）。
2. 配置至少以下变量：
   - `DASHSCOPE_API_KEY`（使用 DashScope 时必填）
   - `FEISHU_APP_ID`、`FEISHU_APP_SECRET`（启用飞书机器人时）
   - `DINGTALK_APP_KEY`、`DINGTALK_APP_SECRET`（启用钉钉机器人时）
3. 启动：
   - Windows：`start.bat`
   - macOS/Linux：`docker-compose up -d`
4. 访问应用：`http://localhost:8080`

停止服务：
- Windows：`stop.bat`
- macOS/Linux：`docker-compose down`

### 方式二：本地 Java 启动

前提：
- 已安装 Java 21
- 已安装 Maven（可选，若已存在可执行 JAR 则可不装）
- 已准备可访问的 Milvus 与模型服务（DashScope 或 Ollama）

步骤：
1. 构建项目：`mvn clean package -DskipTests`
2. 启动应用：
   - Windows：`java -jar target/rag-knowledge-base-1.0.0.jar`
   - macOS/Linux：`chmod +x start.sh && ./start.sh`
3. 停止应用（macOS/Linux）：`chmod +x stop.sh && ./stop.sh`

## 核心配置说明

配置文件：`src/main/resources/application.yml`

- `llm.mode`：`dashscope` 或 `ollama`
- `embedding.mode`：`dashscope` 或 `ollama`
- `milvus.host` / `milvus.port`：Milvus 地址
- `rag.chunk.max-segment-size`：分块大小
- `rag.chunk.max-overlap-size`：分块重叠
- `rag.retrieval.max-results`：检索返回条数
- `rag.retrieval.min-score`：检索最低相似度阈值
- `feishu.app.*`：飞书应用凭证
- `dingtalk.app.*`：钉钉应用凭证

> 建议使用环境变量覆盖敏感配置，不要将真实密钥提交到仓库。

## 接口说明（核心）

基础路径：`/api`

- `POST /api/upload`：上传单个 PDF
- `POST /api/upload/batch`：批量上传 PDF
- `POST /api/query`：非流式问答
- `GET /api/health`：健康检查

管理后台路径：`/admin`

- `/admin/upload`：文档上传页面
- `/admin/chat`：问答测试页面
- `/admin/documents`：文档管理页面
- `POST /admin/api/query/stream`：流式问答（SSE）

## 机器人集成

- 飞书：通过 `feishu.app.app-id` / `feishu.app.app-secret` 配置并对接事件订阅
- 钉钉：通过 `dingtalk.app.app-key` / `dingtalk.app.app-secret` 配置并对接消息流

## 常见问题

- **启动后无法检索到结果**：检查 Milvus 连通性、Embedding 维度与模型是否匹配。
- **回答速度慢**：可降低 `rag.retrieval.max-results`，或切换更快的模型。
- **Docker 启动失败**：先执行 `docker-compose logs -f` 查看依赖服务是否健康。

## Web 效果图

![登录页](images/login_img.png)
![文档上传页](images/upload_img.png)
![文档管理页](images/doc_mng.png)
![问题回答页](images/question_img.png)

## 飞书机器人效果图

![飞书机器人效果图](images/feishu_img.png)

## 钉钉机器人效果图

![钉钉机器人效果图](images/dingtalk_img.png)