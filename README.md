# 智循书堂 · 二手教材循环交易与智能管理系统

> A full-stack second-hand textbook trading & intelligent management system, integrated with an AI-powered Q&A assistant.
>
> Vue3 + SpringBoot3 + FastAPI · RAG + Function Calling · Multi-model fallback

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-42B883?logo=vuedotjs&logoColor=white)](https://vuejs.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.110+-009688?logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/)
[![MySQL](https://img.shields.io/badge/MySQL-5.7%2B-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/license-仅供学习-blue)](#-license)

## ✨ 项目亮点

- **AI 智能问答**：基于书籍元数据的 RAG 检索链路（BM25 + 大模型生成），自然语言找书与智能推荐
- **Function Calling**：AI 调用库存查询工具，联动实时数据
- **多轮对话**：上下文管理，支持连续追问
- **多模型容灾**：DeepSeek + 智谱 GLM 自动 fallback
- **全栈实现**：Vue3 + SpringBoot3 + MyBatis-Plus，事务处理与核心 SQL
- **完整工程化**：从接口设计、数据库建模、模块拆分到本地一键启动

## 🏗️ 架构

```
┌─────────────────┐    /api/*    ┌──────────────────────┐
│  Frontend Vue3   │ ───────────▶ │  Backend SpringBoot   │
│  :5173           │             │  :8080                │
│  (Vite dev)      │             │  (MyBatis-Plus+MySQL) │
└────────┬─────────┘             └──────────────────────┘
         │  /ai/*  (Vite proxy → :8000, strips /ai prefix)
         ▼
┌──────────────────────┐
│  AI Service FastAPI  │  ──▶  MySQL (book corpus + stock)
│  :8000               │  ──▶  LLMs (DeepSeek / 智谱 / … fallback)
│  (BM25 retrieval + LLM)
└──────────────────────┘
```

| 服务 | 端口 | 启动命令 | 必需 |
|------|------|---------|------|
| MySQL | 3306 | 系统服务 / XAMPP | ✅ |
| Backend (SpringBoot) | 8080 | `mvn spring-boot:run` | ✅ |
| AI Service (FastAPI) | 8000 | `uvicorn main:app --reload` | ✅（AI 对话） |
| Frontend (Vue3) | 5173 | `npm run dev` | ✅ |

> 三个服务（MySQL / Backend / AI Service / Frontend）必须**同时运行**，前端才能联调通。

## 📁 目录结构

```
book/
├── backend/            # SpringBoot 后端（端口 8080）
│   ├── pom.xml
│   └── src/main/resources/application.yml   # 修改 DB/JWT 配置
├── frontend/           # Vue3 前端（端口 5173）
│   ├── package.json
│   ├── vite.config.js  # 代理：/api → 8080，/ai → 8000
│   └── src/components/AiChat.vue            # 右下角 AI 对话窗
├── ai-service/         # FastAPI AI 微服务（端口 8000）
│   ├── main.py / llm.py / retriever.py / db.py / config.py
│   ├── requirements.txt
│   ├── .env.example    # 复制为 .env 填入 API key
│   └── README.md
├── database/           # 7 个建表 SQL（book_cycle_*.sql）
├── 启动文档.md          # 中文版详细启动指南
├── .gitignore
└── README.md           # 本文件
```

## 🚀 快速启动

> 详细步骤见 [`启动文档.md`](./启动文档.md)。本节为最小可运行流程。

### 环境要求（最低版本）

| 组件 | 版本 | 验证命令 |
|------|------|---------|
| JDK | **17** | `java -version` |
| Maven | 3.8+ | `mvn -v` |
| MySQL | 5.7+ | 服务管理器 |
| Python | 3.10+ | `python --version` |
| Node.js | 18+ | `node -v` |

### 1. 准备数据库

```powershell
net start MySQL80
mysql -uroot -p<password> -e "CREATE DATABASE IF NOT EXISTS book_cycle CHARACTER SET utf8mb4;"
Get-ChildItem database\book_cycle_*.sql | ForEach-Object {
    mysql -uroot -p<password> book_cycle < $_.FullName
}
```

### 2. 启动后端 (SpringBoot)

```bash
cd backend
# 编辑 src/main/resources/application.yml，配置 DB 密码与 JWT 密钥
mvn spring-boot:run
```

### 3. 启动 AI 服务 (FastAPI)

```bash
cd ai-service
cp .env.example .env
# 编辑 .env，填入 DEEPSEEK_API_KEY / ZHIPU_API_KEY
pip install -r requirements.txt
uvicorn main:app --reload
```

### 4. 启动前端 (Vue3)

```bash
cd frontend
npm install
npm run dev
```

打开 <http://localhost:5173>，右下角可使用 AI 对话。

## 🛠️ 技术栈

**Frontend** · Vue 3 · Vite · Vue Router · Pinia · Element Plus · Axios

**Backend** · Spring Boot 3.x · Spring MVC · MyBatis-Plus · MySQL · JWT · Lombok

**AI Service** · FastAPI · Pydantic · SQLAlchemy · BM25 (rank-bm25) · OpenAI SDK · Requests

**Tooling** · Maven · npm · Git

## 🔐 环境变量

`ai-service/.env` **不入仓**（已在 `.gitignore` 中），请按 `.env.example` 复制并填入：

```env
# LLM 提供商（任选其一或多个用于 fallback）
DEEPSEEK_API_KEY=your_deepseek_key
ZHIPU_API_KEY=your_zhipu_key

# MySQL（与 backend application.yml 保持一致）
DB_HOST=127.0.0.1
DB_PORT=3306
DB_USER=root
DB_PASSWORD=your_password
DB_NAME=book_cycle
```

## 🧪 核心功能演示

- **用户系统**：注册 / 登录 / JWT 鉴权 / 角色权限
- **图书管理**：发布、分类、搜索、详情
- **交易链路**：购物车 → 下单 → 库存扣减 → 订单状态
- **AI 对话窗**（右下角）：
  - "推荐几本计算机网络的书"
  - "这本《算法导论》还有库存吗？"
  - "对比一下这两本书的难度"
- **后台数据**：用户行为埋点、热门书籍统计

## 📝 License

本项目仅供学习与求职作品展示使用，请勿用于商业用途。
