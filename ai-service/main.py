"""智循书堂 · AI 微服务入口（FastAPI）。

启动：uvicorn main:app --host 0.0.0.0 --port 8000 --reload
接口：
  GET  /health              健康检查（含 llm_enabled / 已加载书籍数）
  POST /api/ai/chat        多轮智能问答  body: {session_id, message}
  POST /api/ai/recommend    智能推荐      body: {query?, limit?}
  POST /api/ai/refresh      重新加载书籍语料（书籍变动后调用）
"""
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

from config import settings
from db import load_books
from llm import chat as chat_llm
from retriever import BookRetriever

app = FastAPI(title="智循书堂 AI 服务", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_methods=["*"],
    allow_headers=["*"],
)

retriever = BookRetriever()
# 多轮会话历史：内存存储，仅适合单机 demo；生产请换 Redis 等外部存储。
sessions: dict = {}


def reload_books() -> int:
    books = load_books()
    retriever.build(books)
    return len(books)


reload_books()


def book_card(b: dict) -> dict:
    return {
        "id": b.get("id"),
        "title": b.get("title"),
        "author": b.get("author"),
        "price": b.get("price"),
        "condition_level": b.get("condition_level"),
        "description": (b.get("description") or "")[:80],
    }


class ChatRequest(BaseModel):
    session_id: str = "default"
    message: str


class RecommendRequest(BaseModel):
    query: str = ""
    limit: int = 6


@app.get("/health")
def health():
    return {
        "status": "ok",
        "llm_enabled": settings.llm_enabled,
        "providers": settings.provider_names,
        "active_model": settings.active_model,
        "books_loaded": len(retriever.books),
    }


@app.post("/api/ai/chat")
def chat(req: ChatRequest):
    if not req.message.strip():
        raise HTTPException(status_code=400, detail="message 不能为空")

    history = sessions.get(req.session_id, [])
    sources = retriever.search(req.message)
    reply, new_history, used, chain = chat_llm(history, req.message, sources)
    sessions[req.session_id] = new_history

    return {
        "reply": reply,
        "sources": [book_card(b) for b in sources],
        "session_id": req.session_id,
        "provider": used,
        "provider_chain": chain,
    }


@app.post("/api/ai/recommend")
def recommend(req: RecommendRequest):
    sources = retriever.search(req.query, top_k=req.limit) if req.query.strip() else []
    if not sources:
        # 无查询词或无结果时，回退到热门（按交易量降序）
        sources = sorted(
            retriever.books, key=lambda b: b.get("trade_count") or 0, reverse=True
        )[: req.limit]
    return {"books": [book_card(b) for b in sources]}


@app.post("/api/ai/refresh")
def refresh():
    n = reload_books()
    return {"reloaded": n}


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(app, host="0.0.0.0", port=8000)
