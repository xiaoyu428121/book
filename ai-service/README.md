# 智循书堂 · AI 微服务

基于 **RAG（检索增强生成）** 的二手教材智能问答 / 推荐服务，接入 **多平台大模型**（OpenAI 兼容协议，DeepSeek 为主、智谱/硅基流动/阿里百炼自动兜底）。
把简历里"待做"的 AI 项目真正落到了这个系统里：自然语言找书、智能推荐、可联动库存查询。

---

## 一、架构

```
┌─────────────┐     /ai/*      ┌──────────────────┐    MySQL     ┌──────────┐
│  前端 Vue3  │ ─────────────▶ │  AI 服务(FastAPI) │ ──────────▶│ book_cycle│
│ (悬浮对话窗)│ ◀───────────── │  Python :8000     │            └──────────┘
└─────────────┘    JSON       └──────────────────┘
                                    │ 检索(BM25) + 生成(DeepSeek)
                                    ▼
                              Function Calling → query_book_stock（实时库存）
```

设计要点：
- **独立微服务**，与 SpringBoot 后端（:8080）解耦，前端通过 Vite 代理 `/ai` 转发。
- **检索器可插拔**：默认用 BM25 稀疏检索（离线、零依赖、适合千级书籍语料）；预留接口可平滑升级为"稠密向量 + Chroma"。
- **多轮上下文**由服务端按 `session_id` 维护（当前内存存储，生产请换 Redis）。
- **Function Calling**：用户问"还有货吗/多少钱"时，模型自动调用 `query_book_stock` 查实时库存再回答。
- **多模型自动 fallback**：按优先级装配所有已配置 Key 的平台（DeepSeek → 智谱 → 硅基流动 → 阿里百炼），主模型额度耗尽或请求失败时**自动切下一个**，无需人工干预；全部不可用时降级为检索式回复，对话不报错。

---

## 二、目录结构

```
ai-service/
├── config.py        # 配置中心（全部走环境变量，禁止硬编码）
├── db.py           # MySQL：加载书籍语料 + 库存查询
├── retriever.py    # BM25 检索器
├── llm.py          # 多提供方调用 + Function Calling + 多轮 + 自动 fallback
├── main.py         # FastAPI 入口与接口
├── requirements.txt
├── .env.example   # 复制为 .env 并填入你的 Key
└── README.md
```

---

## 三、环境准备

要求 Python 3.10+。建议使用虚拟环境：

```bash
cd ai-service
python -m venv .venv
source .venv/Scripts/activate        # Windows
pip install -r requirements.txt
cp .env.example .env                # 然后编辑 .env 填入 DEEPSEEK_API_KEY
```

`.env` 关键配置（主模型必填，备用模型按免费额度选填）：

| 变量 | 说明 | 默认 / 免费政策 |
|------|------|------|
| `DEEPSEEK_API_KEY` | 主模型，sk- 开头，DeepSeek 平台获取 | 空（空则跳过） |
| `DEEPSEEK_BASE_URL` | 兼容地址，含 `/v1` | `https://api.deepseek.com/v1` |
| `DEEPSEEK_MODEL` | 模型名 | `deepseek-chat` |
| `ZHIPU_API_KEY` / `*_BASE_URL` / `*_MODEL` | 备用1：智谱 GLM | `glm-4-flash` **长期免费** |
| `SILICONFLOW_API_KEY` / `*_BASE_URL` / `*_MODEL` | 备用2：硅基流动 | 注册送约 2000 万免费 tokens |
| `DASHSCOPE_API_KEY` / `*_BASE_URL` / `*_MODEL` | 备用3：阿里百炼 | 通义千问新用户免费额度 |
| `BOOK_DB_*` | 连接 book_cycle 数据库 | localhost/3306/root/0428 |
| `TOP_K` | 每次检索召回数量 | 5 |
| `CORS_ORIGINS` | 允许的前端源 | `*` |

调用顺序：**DeepSeek → 智谱 → 硅基流动 → 阿里百炼**；任一平台响应正常即用，否则自动下一个；全部失败降级为检索式回复。

> 没有 API Key 也能启动：服务会自动降级为"纯检索 + 规则回复"，方便先联调前端与检索。只需填一个平台 Key 即可点亮大模型能力；建议至少再填一个免费平台（如智谱 `glm-4-flash`）作兜底。

---

## 四、启动与联调

1. 启动 AI 服务（默认 :8000）：
   ```bash
   uvicorn main:app --host 0.0.0.0 --port 8000 --reload
   ```
2. 启动前端（`frontend`，Vite :5173，已配置 `/ai` 代理到 :8000）。
3. 右下角出现 🤖 悬浮按钮，点开即可对话。

接口：
- `GET  /health` — 健康检查（返回 `llm_enabled` / `providers` 列表 / `active_model` / 已加载书籍数）
- `POST /api/ai/chat` — `{session_id, message}` → `{reply, sources, session_id, provider}`（`provider` 为实际命中的平台名，便于排查）
- `POST /api/ai/recommend` — `{query?, limit?}` → 相关/热门书籍
- `POST /api/ai/refresh` — 书籍变动后重新加载语料

---

## 五、本次质量提升记录（团队参考）

| 项 | 改动 | 文件 |
|----|------|------|
| **SQL 注入修复** | `BookController.list` 由字符串拼接改为 `?` 占位符参数化 | backend `BookController.java` |
| **密钥外置** | DB 密码、JWT 秘钥改为 `${ENV:默认}`，禁止明文入库 | backend `application.yml` |
| **关闭堆栈泄露** | `server.error` 的 stacktrace/exception 设为不向前端暴露 | backend `application.yml` |
| **接上死代码** | 已实现但未暴露的 `recommendByItemCF`（物品协同过滤）新增端点 | backend `RecommendController.java` |
| **建立测试文化** | 新增 Mockito 单元测试（无需 DB 即可验证推荐降级逻辑） | backend `src/test/.../RecommendServiceImplTest.java` |
| **前端 AI 对话** | 新增 `/ai` 代理、`api/ai.js`、悬浮 `AiChat.vue` 并挂载到 `App.vue` | frontend |
| **构建修复** | 补充 `terser` 依赖，`npm run build` 可正常产出 | frontend `package.json` |
| **多模型 fallback** | `config.py` 升级为「主+3 备用」提供方体系；`llm.py` 遍历调用、失败自动切下一个、全失败降级检索回复；`/health`、chat 响应透出实际平台 | ai-service `config.py` / `llm.py` / `main.py` |

> 后端编译（本机需 Maven）：`cd backend && mvn compile` / `mvn test -Dtest=RecommendServiceImplTest`

---

## 六、后续可演进

1. 检索升级为 **稠密向量（bge 中文 embedding + Chroma）**，混合 BM25 做 hybrid retrieval。
2. 多轮历史改存 **Redis**，支持多实例部署。
3. AI 服务增加 **JWT 鉴权**，与前端登录态打通。
4. 推荐结果接入详情页"相关推荐"，替换现有规则推荐。
