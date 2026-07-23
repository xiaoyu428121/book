"""MySQL 数据访问：加载书籍语料 + 实时库存查询（供 Function Calling 调用）。"""
from decimal import Decimal

import pymysql

from config import settings


def _jsonable(row: dict) -> dict:
    """将 MySQL 返回的 Decimal 等类型转为 JSON 可序列化（Decimal→float）。

    否则标准库 json.dumps 在把库存结果喂回大模型时会抛
    'Object of type Decimal is not JSON serializable'。
    """
    return {k: float(v) if isinstance(v, Decimal) else v for k, v in row.items()}


def get_conn():
    return pymysql.connect(
        host=settings.DB_HOST,
        port=settings.DB_PORT,
        user=settings.DB_USER,
        password=settings.DB_PASSWORD,
        database=settings.DB_NAME,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )


def load_books():
    """读取在售书籍，构建 RAG 检索语料。"""
    sql = (
        "SELECT id, title, author, category_id, price, original_price, "
        "condition_level, description, status, stock, trade_count "
        "FROM book WHERE status = 1"
    )
    try:
        with get_conn() as conn:
            with conn.cursor() as cur:
                cur.execute(sql)
                return [_jsonable(r) for r in cur.fetchall()]
    except Exception as e:  # noqa: BLE001
        print(f"[db] 加载书籍失败: {e}")
        return []


def query_book_stock(identifier: str):
    """按书名片段或书籍 id 查询实时库存/价格，供大模型工具调用。"""
    try:
        bid = int(identifier)
        sql = "SELECT id, title, price, stock, status FROM book WHERE id = %s"
        param = bid
    except ValueError:
        sql = "SELECT id, title, price, stock, status FROM book WHERE title LIKE %s LIMIT 5"
        param = f"%{identifier}%"

    with get_conn() as conn:
        with conn.cursor() as cur:
            cur.execute(sql, (param,))
            return [_jsonable(r) for r in cur.fetchall()]
