# RAG Knowledge Base System

This is a RAG (Retrieval-Augmented Generation) knowledge base Q&A system based on LangChain4j. It integrates with Feishu bot and supports models from DashScope and Ollama.

## Features

- RAG architecture based on LangChain4j
- Support for multiple large language models (DashScope and Ollama)
- Vector database support (Milvus)
- Web interface management
- Feishu bot integration

## Technology Stack

- Spring Boot 4.0.5
- Java 21
- LangChain4j
- Milvus vector database
- Feishu bot


## Development Guide

1. Ensure Java 21 and Maven are installed
2. Run `mvn clean install` to build the project
3. Start the application:
    - Windows: Run `start.bat`
    - macOS/Linux: Run `./start.sh` (need to grant execute permission first: `chmod +x start.sh`)
4. Stop the application:
    - Windows: Run `stop.bat`
    - macOS/Linux: Run `./stop.sh` (need to grant execute permission first: `chmod +x stop.sh`)


## Web Interface Screenshots
![Login Page](images/login_img.png)

![Document Upload Page](images/upload_img.png)

![Document Management Page](images/doc_mng.png)

![Question Answering Page](images/question_img.png)


## Feishu Bot Screenshot
![Feishu Bot Screenshot](images/feishu_img.png)
