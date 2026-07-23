"""大模型交互：多提供方 OpenAI 兼容协议 + Function Calling + 多轮 + 自动 fallback。

调用策略：
  - 按 settings.providers 优先级依次尝试（DeepSeek → 智谱 → 硅基流动 → 阿里百炼）；
  - 某平台额度耗尽 / 鉴权失败 / 超时，自动切下一个，不阻塞用户；
  - 全部不可用时降级为「检索式规则回复」，保证对话不报错。
"""
import json
import logging

from openai import OpenAI

from config import settings
from db import query_book_stock

logger = logging.getLogger("ai.llm")

SYSTEM_PROMPT = """你是“智循书堂”的二手教材智能助手，帮助高校学生找书、选书、问库存。
严格遵守以下规则：
1. 只基于【检索到的书籍】作答，绝不编造书名、价格或库存。
2. 若检索结果没有相关书籍，如实告知“暂未找到相关教材”，并建议换关键词。
3. 当用户询问“还有货吗 / 库存 / 多少钱 / 有没有”时，必须调用 query_book_stock 工具查询实时数据后再回答。
4. 回答简洁友好，优先列出 2-4 本最相关的书，并说明推荐理由。
"""

TOOLS = [
    {
        "type": "function",
        "function": {
            "name": "query_book_stock",
            "description": "查询某本书的实时库存与价格（仅上架书籍）。当用户问库存、是否有货、价格时使用。",
            "parameters": {
                "type": "object",
                "properties": {
                    "identifier": {
                        "type": "string",
                        "description": "书名片段或书籍 id",
                    }
                },
                "required": ["identifier"],
            },
        },
    }
]


def _format_sources(sources) -> str:
    if not sources:
        return "（无）"
    lines = []
    for b in sources:
        lines.append(
            f"- 《{b.get('title')}》 作者:{b.get('author')} "
            f"价格:{b.get('price')} 品相:{b.get('condition_level')} id:{b.get('id')}"
        )
    return "\n".join(lines)


def _complete(client: OpenAI, model: str, system: str, history: list, query: str, sources: list) -> str:
    """用指定客户端走完一轮（含 Function Calling 往返），返回回复文本。"""
    messages = [{"role": "system", "content": system}]
    messages += history
    messages.append({"role": "user", "content": query})

    resp = client.chat.completions.create(
        model=model,
        messages=messages,
        tools=TOOLS,
        tool_choice="auto",
        temperature=settings.LLM_TEMPERATURE,
    )
    msg = resp.choices[0].message

    # 处理 Function Calling：先让模型调用工具，再把结果喂回模型
    if msg.tool_calls:
        messages.append(msg)
        for tc in msg.tool_calls:
            if tc.function.name == "query_book_stock":
                args = json.loads(tc.function.arguments)
                stock = query_book_stock(args.get("identifier", ""))
                messages.append(
                    {
                        "role": "tool",
                        "tool_call_id": tc.id,
                        "content": json.dumps(stock, ensure_ascii=False),
                    }
                )
        resp = client.chat.completions.create(
            model=model,
            messages=messages,
            temperature=settings.LLM_TEMPERATURE,
        )
        msg = resp.choices[0].message

    return msg.content or ""


def _rule_reply(history: list, query: str, sources: list, prefix: str) -> tuple:
    """无模型可用时的检索式降级回复。返回 (reply, new_history)。"""
    if not sources:
        reply = f"{prefix}暂未找到相关教材，换个关键词试试？"
    else:
        top = sources[:3]
        reply = prefix + "\n".join(
            f"· 《{b['title']}》 {b.get('price')}元 品相:{b.get('condition_level')}" for b in top
        )
    new_history = history + [
        {"role": "user", "content": query},
        {"role": "assistant", "content": reply},
    ]
    return reply, new_history


def chat(history: list, query: str, sources: list):
    """多轮对话主入口。返回 (回复文本, 更新后的历史, 实际使用的提供方名, 尝试链)。

    提供方名取值：某平台名（deepseek/zhipu/siliconflow/dashscope）、
    "none"（完全未配置任何 Key）、"fallback"（全部平台调用失败，已降级）。
    尝试链 chain：按调用顺序记录的已尝试平台名列表（用于前端演示自动切换）。
    """
    # 无任何可用模型：基于检索结果做规则化降级回复
    if not settings.llm_enabled:
        reply, new_history = _rule_reply(
            history, query, sources, "（当前未配置任何大模型 API Key，仅启用检索。）"
        )
        return reply, new_history, "none", ["none"]

    system = SYSTEM_PROMPT + "\n\n【检索到的书籍】\n" + _format_sources(sources)
    last_err = None
    chain = []
    for prov in settings.providers:
        chain.append(prov.name)
        try:
            client = OpenAI(api_key=prov.api_key, base_url=prov.base_url)
            content = _complete(client, prov.model, system, history, query, sources)
            if not content:
                raise ValueError("模型返回空内容")
            new_history = history + [
                {"role": "user", "content": query},
                {"role": "assistant", "content": content},
            ]
            logger.info("LLM 命中提供方: %s / %s（尝试链: %s）", prov.name, prov.model, chain)
            return content, new_history, prov.name, chain
        except Exception as e:  # noqa: BLE001 —— 任意异常都尝试下一个平台
            last_err = e
            logger.warning("LLM 提供方 %s 失败，尝试下一个: %s", prov.name, e)

    # 全部失败：降级为检索式回复，避免服务直接报错
    reply, new_history = _rule_reply(
        history, query, sources, "（模型调用暂时失败，以下为检索到的相关教材）\n"
    )
    logger.error("所有 LLM 提供方均失败，已降级。最后错误: %s", last_err)
    return reply, new_history, "fallback", chain
