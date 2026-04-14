# RAG 企业知识库问答系统

基于 LangChain4j + Spring Boot + Milvus 的企业级知识库智能问答系统，支持飞书机器人接入。

## 功能特性

- 📄 **知识库文档管理**: 上传企业文档（支持 PDF 格式）
- 🔍 **智能问答**: 基于 RAG 技术的知识库检索和问答
- 🤖 **飞书集成**: 支持通过飞书机器人进行问答
- ☁️ **双模式支持**:
  - 云端模式: 阿里云 DashScope (qwen-plus)
  - 本地模式: Ollama (qwen3)
- 🐳 **Docker 部署**: 一键启动所有服务

## 系统架构

```
用户 -> 飞书机器人 -> Webhook -> Spring Boot 应用
                                    |
                                    v
                              RAG 服务 (LangChain4j)
                                    |
                    +---------------+---------------+
                    |                               |
                    v                               v
            ChatLanguageModel              EmbeddingStore
            (DashScope/Ollama)              (Milvus)
```

## 快速开始

### 前置要求

- Docker & Docker Compose
- JDK 17+ (本地开发)
- Maven 3.6+ (本地开发)

### 配置环境变量

1. 复制环境变量示例文件:
```bash
copy .env.example .env
```

2. 编辑 `.env` 文件，填入你的配置:

```env
# 阿里云 DashScope API Key
DASHSCOPE_API_KEY=your-api-key

# 飞书应用配置
FEISHU_APP_ID=your-app-id
FEISHU_APP_SECRET=your-app-secret
FEISHU_VERIFICATION_TOKEN=your-verification-token

# 管理后台登录配置 (可选，有默认值)
ADMIN_USERNAME=admin
ADMIN_PASSWORD=admin123
```

### 获取 API Key

#### 阿里云 DashScope
1. 访问: https://dashscope.console.aliyun.com/
2. 注册/登录账号
3. 创建 API Key

#### 飞书应用
1. 访问: https://open.feishu.cn/
2. 创建企业自建应用
3. 获取 App ID, App Secret, Verification Token
4. 配置事件订阅 URL: `http://your-domain/api/feishu/event`

### Docker 部署 (推荐)

#### Windows 启动
```bash
start.bat
```

#### Linux/Mac 启动
```bash
docker-compose up -d
```

#### 查看日志
```bash
docker-compose logs -f rag-knowledge-base
```

#### 停止服务
```bash
# Windows
stop.bat

# Linux/Mac
docker-compose down
```

### 本地开发

#### 1. 启动 Milvus (Docker)
```bash
docker-compose up -d milvus-standalone etcd minio
```

#### 2. 编译项目
```bash
mvn clean package -DskipTests
```

#### 3. 运行应用
```bash
java -jar target/rag-knowledge-base-1.0.0.jar
```

或使用 IDE 直接运行 `RagKnowledgeBaseApplication.java`

## 管理后台

### 登录访问

启动应用后，访问: `http://localhost:8080/login`

**默认账号**:
- 用户名: `admin`
- 密码: `admin123`

> ⚠️ 请在生产环境中修改默认密码，通过环境变量 `ADMIN_USERNAME` 和 `ADMIN_PASSWORD` 配置

### 管理后台功能

1. **文档上传** (`/admin/upload`)
   - 拖拽或选择 PDF 文件上传
   - 支持批量上传
   - 实时显示上传进度和结果

2. **文档管理** (`/admin/documents`)
   - 查看已上传文档列表
   - 统计信息展示
   - 删除文档

3. **问答测试** (`/admin/chat`)
   - 类聊天界面测试问答
   - 支持多轮对话
   - 打字机效果流式输出
   - Token 消耗统计

## API 接口

### 1. 上传 PDF 文档
```bash
curl -X POST http://localhost:8080/api/upload \
  -F "file=@rules.pdf"
```

### 2. 批量上传 PDF
```bash
curl -X POST http://localhost:8080/api/upload/batch \
  -F "files=@rule1.pdf" \
  -F "files=@rule2.pdf"
```

### 3. 问答接口
```bash
curl -X POST http://localhost:8080/api/query \
  -H "Content-Type: application/json" \
  -d '{"question": "公司的休假政策是什么？"}'
```

### 4. 健康检查
```bash
curl http://localhost:8080/api/health
```

### 5. 飞书 Webhook
```
POST http://localhost:8080/api/feishu/webhook
```

## 配置说明

### 切换 LLM 模式

在 `application.yml` 中修改:

#### 使用云端模型 (DashScope)
```yaml
llm:
  mode: dashscope
  dashscope:
    api-key: your-api-key
    model-name: qwen-plus
```

#### 使用本地模型 (Ollama)
```yaml
llm:
  mode: ollama
  ollama:
    base-url: http://localhost:11434
    model-name: qwen3
```

### Embedding 模型配置

同样支持多种模式:
- `dashscope`: text-embedding-v3
- `ollama`: nomic-embed-text
- 默认: AllMiniLmL6V2 (本地)

## 飞书机器人配置

### 1. 创建应用
1. 登录 [飞书开放平台](https://open.feishu.cn/)
2. 创建企业自建应用
3. 记录 App ID, App Secret

### 2. 配置权限
在飞书开放平台添加以下权限:
- `im:message`
- `im:message:send_as_bot`
- `im:chat`

### 3. 配置事件订阅
- 请求地址: `http://your-domain/api/feishu/event`
- 订阅事件: `im.message.receive_v1`

### 4. 配置机器人
在应用中启用机器人能力，并配置 Webhook 地址

## 项目结构

```
rag-knowledge-base/
├── src/main/java/com/ragknowledge/
│   ├── RagKnowledgeBaseApplication.java    # 主启动类
│   ├── config/
│   │   ├── LlmProperties.java            # LLM 配置
│   │   ├── EmbeddingProperties.java      # Embedding 配置
│   │   ├── MilvusProperties.java         # Milvus 配置
│   │   ├── RagProperties.java            # RAG 配置
│   │   ├── FeishuProperties.java         # 飞书配置
│   │   ├── ChatModelConfig.java          # Chat 模型配置
│   │   ├── EmbeddingModelConfig.java     # Embedding 模型配置
│   │   └── MilvusConfig.java             # Milvus 配置
│   ├── controller/
│   │   └── MainController.java           # REST API 控制器
│   ├── service/
│   │   ├── DocumentService.java          # PDF 解析服务
│   │   ├── RagService.java               # RAG 核心服务
│   │   └── FeishuService.java            # 飞书服务
│   └── dto/
│       ├── FeishuMessage.java            # 飞书消息 DTO
│       └── FeishuResponse.java           # 飞书响应 DTO
├── src/main/resources/
│   └── application.yml                    # 应用配置
├── docker-compose.yml                     # Docker 编排
├── Dockerfile                            # Docker 镜像
├── pom.xml                               # Maven 依赖
├── start.bat                             # 启动脚本
└── stop.bat                              # 停止脚本
```

## 技术栈

- **框架**: Spring Boot 3.2.5
- **AI**: LangChain4j 0.36.2
- **LLM**: 
  - 阿里云 DashScope (qwen-plus)
  - Ollama (qwen3)
- **向量数据库**: Milvus 2.4.6
- **文档解析**: Apache PDFBox
- **容器化**: Docker & Docker Compose

## 常见问题

### 1. Milvus 连接失败
确保 Milvus 服务已完全启动 (通常需要 1-2 分钟)
```bash
docker-compose logs -f milvus-standalone
```

### 2. Ollama 模型无法使用
确保已拉取模型:
```bash
ollama pull qwen3
ollama pull nomic-embed-text
```

### 3. 飞书消息无响应
- 检查 Webhook URL 配置是否正确
- 查看应用日志: `docker-compose logs -f rag-knowledge-base`
- 确认飞书应用权限已配置

### 4. PDF 解析失败
- 确保 PDF 文件未加密
- 检查 PDF 格式是否标准

## 性能优化建议

1. **向量检索**: 调整 `rag.retrieval.max-results` 和 `min-score`
2. **文档分块**: 调整 `rag.chunk.max-segment-size` 和 `max-overlap-size`
3. **对话记忆**: 修改 `MessageWindowChatMemory` 大小
4. **JVM 参数**: 调整 `JAVA_OPTS` 环境变量

## 开发计划

- [ ] 支持更多文档格式 (Word, Markdown)
- [ ] 添加文档管理界面
- [ ] 多轮对话优化
- [ ] 问答历史记录
- [ ] 用户权限管理
- [ ] 监控和告警

## 许可证

MIT License

## 联系方式

如有问题，请提交 Issue 或联系开发者。
