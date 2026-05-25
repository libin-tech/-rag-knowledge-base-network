# AGENTS.md

本文件记录项目级别的通用规范。后端详细规范见 [backend/CLAUDE.md](backend/CLAUDE.md)，前端详细规范见 [frontend/CLAUDE.md](frontend/CLAUDE.md)。

如果本文件与仓库中的脚本、工作流、代码现状不一致，以实际可执行内容为准，并在相关改动中顺手修正文档。

# 项目概述

RAG 企业知识库问答系统，前后端分离架构。

# 技术栈

| 端 | 核心技术 |
|---|---|
| 后端 | Java 21 / Spring Boot / MyBatis-Plus / Sa-Token / LangChain4j / PostgreSQL |
| 前端 | Vue 3 / Ant Design Vue 4 / Vite 5 / Axios |

# 目录结构

```
rag-knowledge-base-network/
├── backend/                 # 后端（Spring Boot Maven 项目）
│   ├── src/                 # 后端源码
│   ├── pom.xml              # Maven 配置
│   ├── Dockerfile           # 后端容器构建
│   └── CLAUDE.md            # 后端开发规范
├── frontend/                # 前端（Vue 3 Vite 项目）
│   ├── src/                 # 前端源码
│   ├── package.json         # 前端依赖
│   ├── vite.config.js       # Vite 配置
│   └── CLAUDE.md            # 前端开发规范
├── .doc/
│   ├── db/                  # 数据库脚本
│   └── images/              # 文档图片
├── docker-compose.yml       # 容器编排
├── AGENTS.md                # 本文件 —— 项目通用规范
└── CLAUDE.md                # 子规范引用清单
```

# 通用代码质量规范

以下规则同时适用于前端和后端：

- 无用的代码、注释、文件、目录、配置文件、依赖必须删除。
- 重复逻辑必须抽离成方法或组合式函数。
- 禁止出现硬编码，使用常量代替。
- 禁止写入密钥、密码等敏感信息到代码中，使用环境变量或配置文件管理。

# 分支规范

- 默认分支：`master`
- 需求分支：`feature/xxx`
- 迭代分支：`iteration/xxx`
- 缺陷分支：`bugfix/xxx`
