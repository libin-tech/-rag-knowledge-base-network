# RAG Knowledge Base System

[中文](README.md) | English

A LangChain4j-based RAG (Retrieval-Augmented Generation) knowledge base system with web admin panel, REST APIs, streaming Q&A, and bot integrations for Feishu and DingTalk.

## Features

- **End-to-end RAG pipeline**: PDF parsing, chunking, embedding, Milvus retrieval, LLM generation
- **Multi-model support**: switchable DashScope (cloud), Ollama (local), OpenAI-compatible models
- **Dual access**: web admin pages + REST APIs
- **Streaming Q&A**: SSE support
- **Bot integrations**: Feishu and DingTalk robots
- **Observability**: key-stage logging with retrieval-enhancement timing
- **Model configuration**: real-time LLM/Embedding config via admin UI
- **Message channels**: enable/disable Feishu and DingTalk robots
- **Knowledge base management**: create, enable/disable, switch multiple knowledge bases

## Tech Stack

### Backend
- Java 21
- Spring Boot 4.0.5
- Sa-Token 1.36.0 (authentication)
- LangChain4j 0.36.2
- MyBatis-Plus 3.5.15
- PostgreSQL
- Milvus 2.4.x
- Hutool 5.8.x

### Frontend
- Vue 3
- Ant Design Vue 4
- Vite 5
- Axios
- Marked.js + Highlight.js

### Deployment
- Docker / Docker Compose

## Project Structure

```
rag-knowledge-base-network/
├── frontend/                              # Vue 3 frontend project
│   ├── src/
│   │   ├── api/                          # API layer (Axios + Sa-Token interceptor)
│   │   ├── router/                       # Route config (with auth guard)
│   │   └── views/
│   │       ├── Login.vue                 # Login page
│   │       └── admin/
│   │           ├── Layout.vue            # Admin layout (sidebar + header)
│   │           ├── Upload.vue            # Document upload
│   │           ├── Documents.vue         # Document management
│   │           ├── Chat.vue              # Q&A testing (SSE streaming)
│   │           ├── Config.vue            # Model configuration
│   │           ├── Channel.vue           # Message channel configuration
│   │           └── KnowledgeBase.vue     # Knowledge base management
│   ├── package.json
│   └── vite.config.js
│
├── src/main/java/com/bin/ragknowledge/
│   ├── controller/     # API controllers
│   ├── service/        # Core business logic
│   ├── repository/     # Data access layer
│   │   ├── mapper/    # Mapper interfaces
│   │   └── entity/    # Entity classes (DO)
│   ├── config/        # Configuration classes (including Sa-Token)
│   ├── context/       # Context (ThreadLocal, etc.)
│   └── enums/         # Enum constants
│
└── src/main/resources/
    ├── application.yml       # System configuration
    └── static/               # Frontend build output (Vue build target)
```

Core resources:
- `frontend/`: Vue 3 frontend source code, builds to `src/main/resources/static/`
- `src/main/resources/application.yml`: system configuration
- `docker-compose.yml`: all-in-one orchestration for Milvus + MinIO + Etcd + app

## Quick Start

### Option 1: Docker Compose (Recommended)

1. Prepare `.env` file (see `.env.example`)
2. Configure environment variables
3. Build frontend: `cd frontend && npm install && npm run build`
4. Start services:
   - Windows: `start.bat`
   - macOS/Linux: `docker-compose up -d`
5. Access: `http://localhost:8080`

Stop services:
- Windows: `stop.bat`
- macOS/Linux: `docker-compose down`

### Option 2: Local Java Runtime

Prerequisites:
- Java 21 installed
- Maven installed
- Node.js 18+ installed
- Accessible Milvus and model service

Steps:
1. Build frontend: `cd frontend && npm install && npm run build`
2. Build backend: `mvn clean package -DskipTests`
3. Run: `java -jar target/rag-knowledge-base-1.1.0.jar`
4. Stop: terminate the process

### Option 3: Frontend Dev Mode (Separate Dev Servers)

1. Start backend: `mvn spring-boot:run`
2. Start frontend dev server: `cd frontend && npm run dev`
3. Access `http://localhost:3000`, API requests auto-proxy to backend at port 8080

## Key Configuration

### Backend Configuration

File: `src/main/resources/application.yml`

| Config | Description |
|--------|------------|
| `milvus.host` / `milvus.port` | Milvus endpoint |
| `rag.chunk.max-segment-size` | chunk size |
| `rag.chunk.max-overlap-size` | chunk overlap |
| `rag.retrieval.max-results` | max retrieved segments |
| `rag.retrieval.min-score` | similarity threshold |
| `auth.admin-username` | admin username |
| `auth.admin-password` | admin password |

### Authentication

Uses **Sa-Token** for token-based authentication:

- Frontend calls `POST /api/auth/login` to obtain a token
- Token is sent via the **Authorization** header
- Default token validity: 24 hours (configurable)
- Activity timeout: 30 minutes (auto-logout after inactivity)

Sa-Token config (`sa-token` prefix in `application.yml`):

| Config | Default | Description |
|--------|---------|-------------|
| `token-name` | `Authorization` | Token header name |
| `timeout` | `86400` | Token TTL (seconds) |
| `activity-timeout` | `1800` | Activity timeout (seconds) |
| `is-concurrent` | `true` | Allow concurrent logins |
| `is-read-head` | `true` | Read token from header |
| `is-read-cookie` | `false` | Don't read token from cookie |

### .env Configuration

Refer to `.env.example` for all required environment variables:

| Variable | Description |
|----------|-------------|
| `POSTGRES_*` | PostgreSQL connection |
| `MILVUS_*` | Milvus vector DB connection |
| `MINIO_*` | MinIO file storage |
| `ADMIN_USERNAME` | Admin username (default: admin) |
| `ADMIN_PASSWORD` | Admin password (default: admin@2026) |

> Use environment variables for secrets. Do not commit real keys to the repository.

## Admin Panel

Access `http://localhost:8080/login` to enter the admin panel:

| Page | Path | Description |
|------|------|------------|
| Document Upload | `/admin/upload` | PDF upload and parsing |
| Document Management | `/admin/documents` | uploaded document list |
| Q&A Test | `/admin/chat` | RAG Q&A testing (SSE streaming) |
| Model Config | `/admin/config` | LLM/Embedding config |
| Message Channel | `/admin/channel` | Feishu/DingTalk config |
| Knowledge Base | `/admin/knowledge-base` | create, enable/disable, switch knowledge bases |

### Knowledge Base Management

Access `/admin/knowledge-base` to manage knowledge bases:

- **Create knowledge base**: custom name and description
- **Enable/disable**: control knowledge base status
- **Default knowledge base**: built-in default knowledge base (ID: default)

### Model Configuration

Access `/admin/config` to manage LLM and Embedding models:

- **LLM Configuration**: supports DashScope, Ollama, OpenAI modes
- **Embedding Configuration**: supports DashScope, Ollama, OpenAI modes

Each mode supports independent API Key, endpoint, model name, timeout configuration with real-time switching.

### Message Channels

Access `/admin/channel` to configure message channels:

- **Feishu Bot**: appId + appSecret
- **DingTalk Bot**: clientId + clientSecret

Supports enable/disable, config auto-takes effect after save.

## API Reference

### Authentication APIs

| API | Method | Description |
|-----|--------|-------------|
| `/api/auth/login` | POST | Login, returns token |
| `/api/auth/logout` | GET | Logout |
| `/api/auth/info` | GET | Get current user info |

### Public APIs

Base path: `/api`

| API | Method | Description |
|------|-------|------------|
| `/api/upload` | POST | upload single PDF |
| `/api/upload/batch` | POST | batch upload PDFs |
| `/api/query` | POST | non-streaming Q&A |
| `/api/health` | GET | health check |

### Admin APIs

Base path: `/admin`

| API | Method | Description |
|------|-------|------------|
| `/admin/api/knowledge-base/list` | GET | knowledge base list |
| `/admin/api/knowledge-base` | POST | create knowledge base |
| `/admin/api/knowledge-base/{id}` | PUT | update knowledge base |
| `/admin/api/knowledge-base/{id}` | DELETE | delete knowledge base |
| `/admin/api/query/stream` | POST | streaming Q&A (SSE) |
| `/admin/api/documents` | GET | document list |
| `/admin/api/document/{id}` | DELETE | delete document |
| `/admin/api/document/{id}/preview` | GET | preview document |
| `/admin/api/config/llm` | GET/PUT | LLM config |
| `/admin/api/config/embedding` | GET/PUT | Embedding config |
| `/admin/api/channel/list` | GET | channel list |
| `/admin/api/channel/{type}` | GET/PUT | channel detail/update |

> Admin APIs require the Authorization header for authentication.

## FAQ

- **No retrieval results after upload**: check Milvus connectivity and embedding dimension compatibility.
- **Slow responses**: reduce max results or switch to a faster model.
- **Docker startup issues**: run `docker-compose logs -f` to check service health.
- **Config not taking effect**: refresh page after save, restart app if needed.
- **Login shows "not logged in"**: check that the frontend sends the token correctly in the Authorization header, and verify the token hasn't expired.

## Web Screenshots

![Login Page](.doc/images/login_img.png)
![Knowledge](.doc/images/knowledge_img.png)
![Document Upload Page](.doc/images/upload_img.png)
![Document Management Page](.doc/images/doc_mng.png)
![Q&A Test Page](.doc/images/question_img.png)
![Model Config Page](.doc/images/LLM_CONFIG_img.png)
![Message Channel Page](.doc/images/channel_img.png)

## Feishu Bot Screenshot

![Feishu Bot](.doc/images/feishu_img.png)

## DingTalk Bot Screenshot

![DingTalk Bot](.doc/images/dingtalk_img.png)
