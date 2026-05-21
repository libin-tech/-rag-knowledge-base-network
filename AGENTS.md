# AGENTS.md

本文件用于约束本仓库的默认开发流程，目标是减少重复沟通、减少返工，并让改动和当前项目结构保持一致。

如果本文件与仓库中的脚本、工作流、代码现状不一致，以实际可执行内容为准，并在相关改动中顺手修正文档，避免规则继续漂移。

# 核心框架
- Java 21
- Spring Boot 4.0.5
- Sa-Token 1.36.0
- MyBatis-Plus 3.5.15
- LangChain4j 0.36.2
- Hutool 5.8.13
- Vue 3
- Ant Design Vue 4

# 项目开发规范
## 1. Maven (pom.xml) 规范
- 所有依赖必须指定版本号或通过 dependencyManagement 管理。
- 禁止引入未使用的 starter。

## 2. 项目结构规范
- 遵循标准 Maven 结构：src/main/java/{package}/{module}。
- 前端代码位于 `frontend/` 目录，构建产物输出至 `src/main/resources/static/`。
- controller 层只负责请求转发和参数校验。
- service 层负责业务逻辑。核心业务逻辑在 service 层完成。
- repository 层负责数据库持久化。禁止引入其他持久层框架。禁止处理数据以外的业务。

## 3. MyBatis-Plus 实体规范
- 实体类必须继承 `BaseEntity`。
- 必须使用 Lombok 的 `@Data`, `@AllArgsConstructor` , `@NoArgsConstructor`,`@EqualsAndHashCode(callSuper = true)`,注解。
- 日期时间字段必须使用 `LocalDateTime` 类型。
- 日期字段必须使用 `LocalDate` 类型。
- 时间字段必须使用 `LocalTime` 类型。
- 使用条件构造器方式进行操作。禁止直接写 sql。
- 所有字段必须添加注释。

## 4. 建表语句规范
- 必须包含 `id`, `create_time`, `update_time`, `version`, `creator`, `modifier` 字段。
- 必须有表注释和字段注释。
- 涉及到表变更的需要完善到 `.doc/db/` 目录下。

## 5. 代码风格
- 使用驼峰命名法。
- 核心业务逻辑必须写 JavaDoc 注释。
- 无用的代码、无用的注释、无用的文件、无用的目录、无用的配置文件、无用的图片、无用的文档、无用的测试代码需要删除。
- 禁止使用 `@SuppressWarnings` 注解。
- 禁止使用 `@Deprecated` 注解。
- 重复逻辑必须抽离成方法。
- 逻辑分支超过2个时，必须设计模式。
- 禁止使用 `if` 嵌套 `if`。

## 6. 编码规范
- 禁止使用 `@Autowired` 注解，必须使用构造函数注入。
- 枚举值必须使用 `@AllArgsConstructor` 注解。
- 涉及到通用工具使用，优先使用 `Hutool`提供的工具类。
- 涉及到日志打印的使用 `@Slf4j` 注解。
- 涉及到数据库查询的必须使用MyBatis-Plus的条件构造器。
- 枚举类型使用MyBatis-Plus自动映射枚举。
- 禁止使用线程池，必须使用JDK21的虚拟线程。
- 判空使用 `Hutool`提供的工具类。
- 禁止出现硬编码，使用常量代替。常量类需要单词存放。
- 认证授权使用 Sa-Token，禁止引入其他安全框架。

## 7. 数据库规范
- 使用 PostgreSQL 作为主数据库。
- 脚本存放路径：`.doc/db/V{version}_{description}.sql`

# 分支规范
## 1. 默认分支
默认分支为 `matser`

## 2. 默认开发流程
1. 需求分支：创建 `feature` 分支，命名规则为 `feature/xxx`
2. 迭代分支：创建 `iteration` 分支，命名规则为 `iteration/xxx`
3. 缺陷分支：创建 `bugfix` 分支，命名规则为 `bugfix/xxx`
