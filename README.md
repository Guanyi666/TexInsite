# TexInsite

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.2.0-blue.svg)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.4.5-blue.svg)](https://www.typescriptlang.org/)

TexInsite 是一个现代化的文档管理系统，支持文档上传、智能解析、分享和评论功能。采用前后端分离架构，提供完整的文档协作解决方案。

## ✨ 主要功能

### 📁 文档管理
- **智能上传**：支持多种文档格式（PDF、Word 等），自动提取文本和元数据
- **在线预览**：内置文档预览功能，无需下载即可查看内容
- **版本控制**：软删除和恢复机制，保护重要文档
- **回收站**：已删除文档的管理和恢复

### 🔐 用户认证
- **安全登录**：JWT Token 认证机制
- **用户注册**：简单快捷的账户创建
- **权限控制**：基于角色的访问控制

### 📤 文档分享
- **分享链接**：生成安全的分享链接，支持过期时间设置
- **下载限制**：可设置最大下载次数
- **评论分享**：选择是否在分享时包含评论
- **链接管理**：查看、撤销和管理所有分享链接

### 💬 评论系统
- **文档评论**：为任意文档添加评论
- **分享绑定**：评论可与分享链接关联
- **权限管理**：仅评论创建者可编辑/删除

## 🛠️ 技术架构

### 后端 (TexInsiteBackend)
- **框架**：Spring Boot 3.2.5
- **语言**：Java 21
- **数据库**：PostgreSQL + pgvector（支持向量搜索）
- **认证**：JWT + Spring Security
- **文档解析**：Apache Tika 2.9.2
- **构建工具**：Maven

### 前端 (TexInsiteFrontend)
- **框架**：React 18.2.0
- **语言**：TypeScript 5.4.5
- **构建工具**：Vite 5.1.0
- **路由**：React Router 6.16.0
- **HTTP 客户端**：Axios 1.6.1

### 部署
- **容器化**：Docker + Docker Compose
- **数据库**：PostgreSQL 16 with pgvector extension

## 🚀 快速开始

### 环境要求
- Java 21+
- Node.js 18+
- Docker & Docker Compose
- PostgreSQL 16+ (可选，直接使用 Docker)

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/your-username/texinsite.git
   cd texinsite
   ```

2. **启动数据库**
   ```bash
   cd env
   docker-compose up -d
   ```

3. **后端配置**
   ```bash
   cd ../TexInsiteBackend/TexInsite
   # 修改 src/main/resources/application.yml 中的数据库配置（如果需要）
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

4. **前端配置**
   ```bash
   cd ../../TexInsiteFrontend
   npm install
   npm run dev
   ```

5. **访问应用**
   - 前端：http://localhost:5173
   - 后端 API：http://localhost:8080

## 📖 使用指南

### 用户操作
1. **注册账户**：访问注册页面创建新账户
2. **登录系统**：使用用户名和密码登录
3. **上传文档**：在上传页面选择文件并上传
4. **管理文档**：在仪表板查看、编辑和删除文档
5. **分享文档**：生成分享链接并设置参数
6. **添加评论**：在文档详情页添加评论

### API 文档
后端提供 RESTful API，详细接口文档请参考：
- 认证接口：`/api/auth`
- 文档接口：`/api/documents`
- 分享接口：`/api/share`
- 评论接口：`/api/comments`

## 🏗️ 项目结构

```
TexInsite/
├── env/                          # Docker 配置
│   └── docker-compose.yml
├── TexInsiteBackend/             # Java Spring Boot 后端
│   └── TexInsite/
│       ├── src/main/java/com/texinsite/engine/
│       │   ├── controller/       # REST 控制器
│       │   ├── service/          # 业务逻辑层
│       │   ├── model/            # 数据模型
│       │   ├── repository/       # 数据访问层
│       │   ├── config/           # 配置类
│       │   └── utils/            # 工具类
│       └── src/main/resources/
│           └── application.yml   # 应用配置
├── TexInsiteFrontend/            # React 前端
│   ├── src/
│   │   ├── pages/                # 页面组件
│   │   ├── components/           # 通用组件
│   │   └── api.ts               # API 配置
│   ├── package.json
│   └── vite.config.ts
└── uploads/                      # 文件存储目录
```

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

### 开发规范
- 遵循 Java/Spring Boot 最佳实践
- 使用 TypeScript 进行前端开发
- 编写单元测试
- 更新文档

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系我们

- 项目维护者：Duan Guanyi
- 邮箱：duanguanyi@mail.nwpu.edu.cn
- 项目主页：[[Guanyi666/TexInsite: 文档管理系统](https://github.com/Guanyi666/TexInsite)]

---

⭐ 如果这个项目对你有帮助，请给我们一个 star！