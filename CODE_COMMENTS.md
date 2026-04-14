# 家庭小助手 RAG 系统 - 代码注释说明

## 项目结构概览

```
family-assistant/
├── src/main/java/com/family/assistant/
│   ├── FamilyAssistantApplication.java    # 主启动类
│   ├── config/                            # 配置类目录
│   │   ├── AuthProperties.java           # 认证配置属性
│   │   ├── EmbeddingModelConfig.java     # Embedding 模型配置
│   │   ├── ChatModelConfig.java          # Chat 模型配置
│   │   ├── MilvusConfig.java             # Milvus 数据库配置
│   │   ├── MilvusProperties.java         # Milvus 配置属性
│   │   ├── EmbeddingProperties.java      # Embedding 配置属性
│   │   ├── LlmProperties.java            # LLM 配置属性
│   │   ├── RagProperties.java            # RAG 配置属性
│   │   ├── FeishuProperties.java         # 飞书配置属性
│   │   ├── PasswordEncoderConfig.java    # 密码编码器配置
│   │   └── SecurityConfig.java           # Spring Security 配置
│   ├── controller/                        # 控制器目录
│   │   ├── AdminController.java          # 管理后台控制器
│   │   ├── LoginController.java          # 登录控制器
│   │   └── MainController.java           # API 接口控制器
│   ├── service/                           # 服务层目录
│   │   ├── CustomUserDetailsService.java # 用户详情服务
│   │   ├── DocumentService.java          # PDF 文档解析服务
│   │   ├── FeishuService.java            # 飞书集成服务
│   │   └── RagService.java               # RAG 核心服务
│   └── dto/                               # 数据传输对象
│       ├── FeishuMessage.java            # 飞书消息 DTO
│       └── FeishuResponse.java           # 飞书响应 DTO
├── src/main/resources/
│   ├── application.yml                    # 应用配置文件
│   └── templates/                         # Thymeleaf 模板
│       ├── login.html                     # 登录页面
│       └── admin/                         # 管理后台页面
│           ├── upload.html                # 文档上传页面
│           ├── documents.html             # 文档管理页面
│           └── chat.html                  # 问答测试页面
└── docker-compose.yml                     # Docker 编排文件
```

## 核心类说明

### 1. 配置类 (config 包)

#### AuthProperties
- **作用**: 读取登录认证配置
- **配置项**: admin-username, admin-password

#### SecurityConfig  
- **作用**: Spring Security 安全配置
- **功能**: 
  - 配置 URL 访问权限
  - 配置表单登录
  - 配置登出
  - 公开飞书 Webhook 和健康检查接口

#### PasswordEncoderConfig
- **作用**: 配置密码编码器
- **使用**: BCrypt 强哈希算法

#### CustomUserDetailsService
- **作用**: 自定义用户详情服务
- **功能**: 从配置文件读取管理员账号并进行认证

#### ChatModelConfig
- **作用**: 配置聊天语言模型
- **支持模式**: 
  - dashscope (阿里云 qwen-plus)
  - ollama (本地 qwen3)

#### EmbeddingModelConfig
- **作用**: 配置文本向量化模型
- **支持模式**:
  - dashscope (text-embedding-v3)
  - ollama (nomic-embed-text)
  - 本地 (AllMiniLmL6V2)

#### MilvusConfig
- **作用**: 配置 Milvus 向量数据库连接
- **创建**: EmbeddingStore Bean 用于存储和检索向量

#### LlmProperties / EmbeddingProperties / MilvusProperties / RagProperties / FeishuProperties
- **作用**: 读取 application.yml 中对应的配置项
- **使用**: @ConfigurationProperties 注解自动映射

### 2. 控制器类 (controller 包)

#### LoginController
- **作用**: 处理登录页面路由
- **接口**:
  - GET /login - 显示登录页面
  - 支持错误和登出成功提示

#### AdminController
- **作用**: 管理后台页面和 API
- **页面路由**:
  - GET /admin/upload - 文档上传页面
  - GET /admin/documents - 文档管理页面
  - GET /admin/chat - 问答测试页面
- **API 接口**:
  - POST /admin/api/upload - 单文件上传
  - POST /admin/api/upload/batch - 批量上传
  - POST /admin/api/query - 问答接口
  - GET /admin/api/documents - 获取文档列表
  - DELETE /admin/api/document/{id} - 删除文档

#### MainController
- **作用**: 公共 API 接口和飞书 Webhook
- **接口**:
  - POST /api/upload - 上传 PDF
  - POST /api/upload/batch - 批量上传
  - POST /api/query - 问答
  - POST /api/feishu/webhook - 飞书消息 Webhook
  - POST /api/feishu/event - 飞书事件回调
  - GET /api/health - 健康检查

### 3. 服务类 (service 包)

#### RagService (核心服务)
- **作用**: RAG 核心业务逻辑
- **主要方法**:
  - `addDocument(Document)` - 添加文档到向量库
    1. 文档分割 (递归分块策略)
    2. 添加元数据 (文档 ID)
    3. 向量化 (Embedding 模型)
    4. 存储到 Milvus
  - `query(String)` - 智能问答
    1. 创建检索器 (从向量库检索相关内容)
    2. 构建 AI 助手 (集成聊天模型、对话记忆、检索器)
    3. 生成回答
  - `retrieveRelevantDocuments(String)` - 检索相关文档片段

#### DocumentService
- **作用**: PDF 文档解析
- **主要方法**:
  - `parsePdf(MultipartFile)` - 解析单个 PDF
  - `parseMultiplePdfs(List<MultipartFile>)` - 批量解析 PDF

#### FeishuService
- **作用**: 飞书消息处理和回复
- **主要方法**:
  - `handleMessage(FeishuMessage)` - 处理飞书消息
  - `replyMessage(String, String)` - 回复消息
  - `getTenantAccessToken()` - 获取访问令牌
  - `verifyRequest(String)` - 验证请求来源

#### CustomUserDetailsService
- **作用**: Spring Security 用户详情服务
- **主要方法**:
  - `loadUserByUsername(String)` - 加载用户详情

### 4. 数据传输对象 (dto 包)

#### FeishuMessage
- **作用**: 飞书消息实体
- **结构**:
  - Header - 消息头 (事件 ID、类型、时间等)
  - Event - 事件内容 (发送者、消息内容、聊天 ID 等)
  - Message - 消息详情 (消息 ID、类型、内容等)

#### FeishuResponse
- **作用**: 飞书响应实体
- **字段**: code, msg, data

## 工作流程

### 文档上传流程
```
1. 用户通过管理后台上传 PDF
   ↓
2. DocumentService.parsePdf() 解析 PDF 内容
   ↓
3. RagService.addDocument() 处理文档
   ├── 3.1 文档分割成多个文本块 (DocumentSplitter)
   ├── 3.2 为每个块添加元数据 (文档 ID)
   ├── 3.3 EmbeddingModel 向量化
   └── 3.4 存储到 Milvus 向量数据库
   ↓
4. 返回上传成功结果
```

### 问答流程
```
1. 用户提出问题
   ↓
2. RagService.query() 处理问题
   ├── 2.1 创建检索器 (EmbeddingStoreContentRetriever)
   │   └── 使用 EmbeddingModel 将问题转为向量
   │   └── 在 Milvus 中检索最相关的文本块
   ├── 2.2 构建 AI 助手 (AiServices)
   │   ├── ChatLanguageModel (聊天模型)
   │   ├── ChatMemory (对话记忆)
   │   └── ContentRetriever (内容检索器)
   └── 2.3 AI 助手生成回答
   ↓
3. 返回 AI 回答
```

### 飞书消息处理流程
```
1. 飞书服务器推送消息到 Webhook
   ↓
2. MainController.feishuWebhook() 接收消息
   ↓
3. FeishuService.handleMessage() 处理
   ├── 3.1 解析消息内容
   ├── 3.2 调用 RagService.query() 获取回答
   └── 3.3 构建回复格式
   ↓
4. 返回回复给飞书
```

### 认证流程
```
1. 用户访问 /login 页面
   ↓
2. 输入用户名和密码
   ↓
3. Spring Security 拦截并认证
   ├── 3.1 CustomUserDetailsService.loadUserByUsername()
   ├── 3.2 验证用户名是否匹配配置
   ├── 3.3 PasswordEncoder 验证密码
   └── 3.4 认证成功，创建会话
   ↓
4. 重定向到 /admin/upload
```

## 关键技术点

### 1. LangChain4j RAG 实现
- **文档分割**: DocumentSplitters.recursive() - 递归分块，保持上下文
- **向量化**: EmbeddingModel 将文本转为高维向量
- **检索**: MilvusEmbeddingStore 基于向量相似度检索
- **对话**: AiServices 动态代理集成模型、记忆和检索

### 2. 双模型支持
- **配置驱动**: 通过 llm.mode 配置切换
- **云端模式**: DashScope ChatModel (qwen-plus)
- **本地模式**: Ollama ChatModel (qwen3)

### 3. Spring Security 认证
- **表单登录**: 内置登录页面和认证流程
- **URL 权限**: 分类管理公开和受保护接口
- **密码加密**: BCrypt 强哈希

### 4. Milvus 向量数据库
- **存储**: 文本段和对应的向量
- **检索**: 基于余弦相似度的快速检索
- **过滤**: 支持元数据过滤查询

## 配置说明

### application.yml 主要配置项
```yaml
# LLM 配置
llm:
  mode: dashscope  # 或 ollama
  dashscope:
    api-key: ${DASHSCOPE_API_KEY}
    model-name: qwen-plus
  ollama:
    base-url: http://localhost:11434
    model-name: qwen3

# Embedding 配置
embedding:
  mode: dashscope
  dashscope:
    model-name: text-embedding-v3

# Milvus 配置
milvus:
  host: localhost
  port: 19530
  collection-name: family_rules
  dimension: 1024

# RAG 配置
rag:
  chunk:
    max-segment-size: 500  # 每段最大字符数
    max-overlap-size: 50   # 段间重叠字符数
  retrieval:
    max-results: 5         # 最多检索文档数
    min-score: 0.7         # 最低相关度分数

# 认证配置
auth:
  admin-username: admin
  admin-password: admin123
```

## 部署说明

### Docker 部署
```bash
# 1. 配置环境变量
cp .env.example .env
# 编辑 .env 文件

# 2. 启动服务
docker-compose up -d

# 3. 访问管理后台
http://localhost:8080/login
# 默认账号: admin
# 默认密码: admin123
```

### 本地开发
```bash
# 1. 启动 Milvus (Docker)
docker-compose up -d milvus-standalone etcd minio

# 2. 编译项目
mvn clean package -DskipTests

# 3. 运行应用
java -jar target/family-assistant-1.0.0.jar

# 4. 访问管理后台
http://localhost:8080/login
```

## API 接口文档

### 管理后台接口 (需要登录)
- POST /admin/api/upload - 上传 PDF
- POST /admin/api/upload/batch - 批量上传
- POST /admin/api/query - 问答
- GET /admin/api/documents - 获取文档列表
- DELETE /admin/api/document/{id} - 删除文档

### 公共接口 (无需登录)
- POST /api/upload - 上传 PDF
- POST /api/upload/batch - 批量上传
- POST /api/query - 问答
- POST /api/feishu/webhook - 飞书 Webhook
- GET /api/health - 健康检查

### 页面接口 (需要登录)
- GET /login - 登录页面
- GET /admin/upload - 文档上传页面
- GET /admin/documents - 文档管理页面
- GET /admin/chat - 问答测试页面
