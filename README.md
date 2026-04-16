# RAG知识库系统

[English](README_en.md) | 中文

这是一个基于LangChain4j的RAG（Retrieval-Augmented Generation）知识库问答系统。集成飞书机器人，支持DashScope和Ollama的模型。

## 功能特性

- 基于LangChain4j的RAG架构
- 支持多种大语言模型（DashScope和Ollama）
- 向量数据库支持（Milvus）
- Web界面管理
- 飞书机器人集成

## 技术栈

- Spring Boot 4.0.5
- Java 21
- LangChain4j
- Milvus向量数据库
- Feishu机器人


## 开发指南

1. 确保安装了Java 21和Maven
2. 执行`mvn clean install`构建项目
3. 启动应用：
   - Windows: 运行 `start.bat`
   - macOS/Linux: 运行 `./start.sh` (需要先赋予执行权限: `chmod +x start.sh`)
4. 停止应用：
   - Windows: 运行 `stop.bat`
   - macOS/Linux: 运行 `./stop.sh` (需要先赋予执行权限: `chmod +x stop.sh`)


## Web效果图
![登录页](images/login_img.png)

![文档上传页](images/upload_img.png)

![文档管理页](images/doc_mng.png)

![问题回答页](images/question_img.png)


## 飞书机器人效果图
![飞书机器人效果图](images/feishu_img.png)