# RAG Knowledge Base System

[中文](README.md) | English

This project is a LangChain4j-based RAG (Retrieval-Augmented Generation) knowledge base system. It provides a web admin panel, REST APIs, streaming Q&A, and bot integrations for both Feishu and DingTalk.

## Features

- End-to-end RAG pipeline: PDF parsing, chunking, embedding, Milvus retrieval, LLM generation
- Multi-model support: switch between DashScope (cloud) and Ollama (local)
- Dual access modes: web admin pages and REST APIs
- Streaming Q&A via SSE
- Bot integrations: Feishu and DingTalk
- Observability: key-stage logging including retrieval-enhancement timing

## Tech Stack

- Java 21
- Spring Boot 4.0.5
- LangChain4j 0.36.2
- Milvus 2.4.x
- Hutool 5.8.x
- Docker / Docker Compose

## Project Structure

- `src/main/java/com/bin/ragknowledge/controller`: page and API controllers
- `src/main/java/com/bin/ragknowledge/service`: document parsing and core RAG logic
- `src/main/resources/application.yml`: runtime configuration (model, vector DB, bots)
- `docker-compose.yml`: all-in-one orchestration for Milvus + MinIO + Etcd + app

## Quick Start

### Option 1: Docker Compose (Recommended)

1. Prepare a `.env` file (you can start from `.env.example`).
2. Configure at least:
   - `DASHSCOPE_API_KEY` (required when using DashScope)
   - `FEISHU_APP_ID`, `FEISHU_APP_SECRET` (if enabling Feishu bot)
   - `DINGTALK_APP_KEY`, `DINGTALK_APP_SECRET` (if enabling DingTalk bot)
3. Start services:
   - Windows: `start.bat`
   - macOS/Linux: `docker-compose up -d`
4. Open: `http://localhost:8080`

Stop services:
- Windows: `stop.bat`
- macOS/Linux: `docker-compose down`

### Option 2: Local Java Runtime

Prerequisites:
- Java 21 installed
- Maven installed (optional if the JAR already exists)
- Reachable Milvus and model service (DashScope or Ollama)

Steps:
1. Build: `mvn clean package -DskipTests`
2. Start:
   - Windows: `java -jar target/rag-knowledge-base-1.0.0.jar`
   - macOS/Linux: `chmod +x start.sh && ./start.sh`
3. Stop (macOS/Linux): `chmod +x stop.sh && ./stop.sh`

## Key Configuration

Configuration file: `src/main/resources/application.yml`

- `llm.mode`: `dashscope` or `ollama`
- `embedding.mode`: `dashscope` or `ollama`
- `milvus.host` / `milvus.port`: Milvus endpoint
- `rag.chunk.max-segment-size`: chunk size
- `rag.chunk.max-overlap-size`: chunk overlap
- `rag.retrieval.max-results`: max retrieved segments
- `rag.retrieval.min-score`: retrieval similarity threshold
- `feishu.app.*`: Feishu app credentials
- `dingtalk.app.*`: DingTalk app credentials

> Use environment variables for secrets. Do not commit real keys to the repository.

## API Overview

Base path: `/api`

- `POST /api/upload`: upload one PDF
- `POST /api/upload/batch`: upload multiple PDFs
- `POST /api/query`: non-streaming Q&A
- `GET /api/health`: health check

Admin path: `/admin`

- `/admin/upload`: upload page
- `/admin/chat`: chat test page
- `/admin/documents`: document management page
- `POST /admin/api/query/stream`: streaming Q&A (SSE)

## Bot Integration

- Feishu: configure `feishu.app.app-id` and `feishu.app.app-secret`
- DingTalk: configure `dingtalk.app.app-key` and `dingtalk.app.app-secret`

## FAQ

- **No retrieval results after upload**: check Milvus connectivity and embedding dimension compatibility.
- **Responses are slow**: reduce `rag.retrieval.max-results` or switch to a faster model.
- **Docker startup issues**: inspect logs with `docker-compose logs -f`.

## Web Screenshots

![Login Page](images/login_img.png)
![Document Upload Page](images/upload_img.png)
![Document Management Page](images/doc_mng.png)
![Question Answering Page](images/question_img.png)

## Feishu Bot Screenshot

![Feishu Bot Screenshot](images/feishu_img.png)

## DingTalk Bot Screenshot

![DingTalk Bot Screenshot](images/dingtalk_img.png)
