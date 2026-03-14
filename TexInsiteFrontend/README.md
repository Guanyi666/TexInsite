# TexInsite Frontend

这是 TexInsite 的前端代码（React + Vite）。

## 快速运行

```bash
cd TexInsiteFrontend
npm install
npm run dev
```

默认会在 `http://localhost:5173` 启动，并通过 `vite` 代理把 `/api` 请求转发到后端 `http://localhost:8080`。

## 核心功能
- 登录 / 注册
- 文档上传 + 列表展示
- 生成“过期/可限次下载”分享链接
- 基于文档 ID 的 AI 问答
