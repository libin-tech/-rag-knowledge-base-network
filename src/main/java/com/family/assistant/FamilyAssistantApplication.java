package com.family.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 家庭小助手应用程序主启动类
 * 
 * 这是一个基于 Spring Boot 和 LangChain4j 的 RAG (检索增强生成) 系统
 * 主要功能：
 * 1. PDF 文档上传和解析
 * 2. 文档向量化存储到 Milvus
 * 3. 基于 RAG 的智能问答
 * 4. 飞书机器人集成
 * 
 * @author Family Assistant Team
 * @version 1.0.0
 */
@SpringBootApplication
public class FamilyAssistantApplication {

    /**
     * 应用程序入口方法
     * 
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(FamilyAssistantApplication.class, args);
    }
}
