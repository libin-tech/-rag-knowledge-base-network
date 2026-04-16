# RAG知识库系统

这是一个基于LangChain4j的RAG（Retrieval-Augmented Generation）知识库问答系统。

## 功能特性

- 基于LangChain4j的RAG架构
- 支持多种大语言模型（DashScope和Ollama）
- 向量数据库支持（Milvus）
- Web界面管理

## 技术栈

- Spring Boot 4.0.5
- Java 21
- LangChain4j
- Milvus向量数据库


## 开发指南

1. 确保安装了Java 21和Maven
2. 执行`mvn clean install`构建项目
3. 启动应用：
   - Windows: 运行 `start.bat`
   - macOS/Linux: 运行 `./start.sh` (需要先赋予执行权限: `chmod +x start.sh`)
4. 停止应用：
   - Windows: 运行 `stop.bat`
   - macOS/Linux: 运行 `./stop.sh` (需要先赋予执行权限: `chmod +x stop.sh`)

