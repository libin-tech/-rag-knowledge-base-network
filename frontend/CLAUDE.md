# CLAUDE.md - 前端开发规范

本文件用于约束前端（`frontend/`）的默认开发流程。项目通用规范（目录结构、代码质量、分支规范等）见根目录 [AGENTS.md](../AGENTS.md)。

如果本文件与仓库中的脚本、工作流、代码现状不一致，以实际可执行内容为准，并在相关改动中顺手修正文档。

# 核心框架
- Vue 3（Composition API + `<script setup>`）
- Ant Design Vue 4
- Vue Router 4
- Vite 5
- Axios

# 项目结构

```
frontend/
├── public/
│   └── images/              # 静态图片资源
├── src/
│   ├── api/
│   │   └── index.js         # API 请求封装
│   ├── router/
│   │   └── index.js         # 路由配置
│   ├── views/
│   │   ├── Login.vue        # 登录页
│   │   └── admin/           # 管理后台页面
│   │       ├── Layout.vue
│   │       ├── Upload.vue
│   │       ├── Documents.vue
│   │       ├── Chat.vue
│   │       ├── Config.vue
│   │       ├── Channel.vue
│   │       └── KnowledgeBase.vue
│   ├── App.vue              # 根组件
│   └── main.js              # 入口文件
├── index.html               # HTML 模板
├── package.json
└── vite.config.js           # Vite 配置
```

# 开发规范

## 1. 组件编写规范
- 使用 `<script setup>` 语法糖。
- 使用 Composition API（`ref`、`reactive`、`computed`、`watch` 等）。
- 组件文件使用 PascalCase 命名（如 `Layout.vue`、`KnowledgeBase.vue`）。
- 视图文件按功能模块放入对应子目录（如 `views/admin/`）。

## 2. UI 组件规范
- 统一使用 Ant Design Vue 4 组件库。
- 组件使用 `a-` 前缀（如 `<a-button>`、`<a-table>`、`<a-select>`）。
- 图标使用 `@ant-design/icons-vue`，按需导入。
- 样式优先使用 Ant Design 内置的布局和间距系统，避免硬编码 CSS。

## 3. 路由规范
- 使用 Vue Router 4 的 `createRouter` + `createWebHistory`。
- 管理后台路由统一放在 `/admin` 路径下。
- 需要认证的路由设置 `meta: { requiresAuth: true }`。
- 路由跳转使用 `useRouter().push()` 或命名路由。

## 4. API 请求规范
- 所有 API 请求通过 `src/api/index.js` 中的 Axios 实例发起。
- 请求超时设置为 60 秒。
- 认证 token 通过 `Authorization` 请求头传递，token 值存储在 `localStorage` 的 `satoken` 键中。
- 401 响应自动清除 token 并跳转登录页（排除 `/api/auth/login` 路径）。
- 响应拦截器已做 `response.data` 解包，调用方直接获取数据对象。

## 5. 状态管理规范
- 页面级状态使用 Vue 的 `reactive` / `ref` 管理。
- 跨组件共享状态优先通过 URL 参数或 `localStorage` 传递。
- token 和用户信息存储在 `localStorage` 中。

## 6. 构建配置规范
- 开发服务器端口：`3000`。
- API 代理：`/api` 和 `/admin/api` 代理到 `http://localhost:8081`。
- 构建产物输出至 `../backend/src/main/resources/static`，每次构建清空输出目录。
