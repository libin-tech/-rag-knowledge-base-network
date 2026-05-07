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

- Java 21
- Spring Boot 4.0.5
- LangChain4j 0.36.2
- Milvus 2.4.x
- Hutool 5.8.x
- Docker / Docker Compose

## Project Structure

```
src/main/java/com/bin/ragknowledge/
├── controller/     # Page and API controllers
├── service/       # Core business logic
├── repository/    # Data access layer
│   ├── mapper/   # Mapper interfaces
│   └── entity/   # Entity classes (DO)
├── config/       # Configuration classes
├── filter/       # Filters
├── interceptor/  # Interceptors
├── context/      # Context (ThreadLocal, etc.)
└── enums/        # Enum constants
```

Core resources:
- `src/main/resources/application.yml`: system configuration
- `docker-compose.yml`: all-in-one orchestration for Milvus + MinIO + Etcd + app

## Quick Start

### Option 1: Docker Compose (Recommended)

1. Prepare `.env` file (see `.env.example`)
2. Configure environment variables
3. Start services:
   - Windows: `start.bat`
   - macOS/Linux: `docker-compose up -d`
4. Access: `http://localhost:8080`

Stop services:
- Windows: `stop.bat`
- macOS/Linux: `docker-compose down`

### Option 2: Local Java Runtime

Prerequisites:
- Java 21 installed
- Maven installed
- Accessible Milvus and model service

Steps:
1. Build: `mvn clean package -DskipTests`
2. Run: `java -jar target/rag-knowledge-base-1.0.0.jar`
3. Stop: terminate the process

## Key Configuration

File: `src/main/resources/application.yml`

| Config | Description |
|--------|------------|
| `milvus.host` / `milvus.port` | Milvus endpoint |
| `rag.chunk.max-segment-size` | chunk size |
| `rag.chunk.max-overlap-size` | chunk overlap |
| `rag.retrieval.max-results` | max retrieved segments |
| `rag.retrieval.min-score` | similarity threshold |

## Admin Panel

Access `/admin` to enter the admin panel:

| Page | Path | Description |
|------|------|------------|
| Knowledge Base | `/admin/knowledge-base` | create, enable/disable, switch knowledge bases |
| Document Upload | `/admin/upload` | PDF upload and parsing |
| Document Management | `/admin/documents` | uploaded document list |
| Q&A Test | `/admin/chat` | RAG Q&A testing |
| Model Config | `/admin/config` | LLM/Embedding config |
| Message Channel | `/admin/channel` | Feishu/DingTalk config |

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

> Use environment variables for secrets. Do not commit real keys to the repository.

## API Reference

Base path: `/api`

| API | Method | Description |
|------|-------|------------|
| `/api/upload` | POST | upload single PDF |
| `/api/upload/batch` | POST | batch upload PDFs |
| `/api/query` | POST | non-streaming Q&A |
| `/api/health` | GET | health check |

Admin path: `/admin`

| API | Method | Description |
|------|-------|------------|
| `/admin/api/knowledge-base/list` | GET | knowledge base list |
| `/admin/api/knowledge-base` | POST | create knowledge base |
| `/admin/api/knowledge-base/{id}` | PUT | update knowledge base |
| `/admin/api/knowledge-base/{id}` | DELETE | delete knowledge base |
| `/admin/api/query/stream` | POST | streaming Q&A (SSE) |
| `/admin/api/documents` | GET | document list |
| `/admin/api/document/{id}` | DELETE | delete document |
| `/admin/api/config/llm` | GET/PUT | LLM config |
| `/admin/api/config/embedding` | GET/PUT | Embedding config |
| `/admin/api/channel/list` | GET | channel list |
| `/admin/api/channel/{type}` | PUT | update channel |

## FAQ

- **No retrieval results after upload**: check Milvus connectivity and embedding dimension compatibility.
- **Slow responses**: reduce max results or switch to a faster model.
- **Docker startup issues**: run `docker-compose logs -f` to check service health.
- **Config not taking effect**: refresh page after save, restart app if needed.

## Web Screenshots

![Login Page](images/login_img.png)
![Document Upload Page](images/upload_img.png)
![Document Management Page](images/doc_mng.png)
![Q&A Test Page](images/question_img.png)
![Model Config Page](images/LLM_CONFIG_img.png)
![Message Channel Page](images/channel_img.png)

## Feishu Bot Screenshot

![Feishu Bot](images/feishu_img.png)

## DingTalk Bot Screenshot

![DingTalk Bot](images/dingtalk_img.png)