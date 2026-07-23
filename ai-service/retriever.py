"""检索模块：BM25 稀疏检索（离线、无需 embedding 服务，适合千级书籍语料）。

说明：真正的 RAG 通常由"向量稠密检索"完成。本项目书籍量级小、且 DeepSeek 不提供
embedding 接口，因此用经过验证的 BM25 作为默认检索器，工程上更稳、零外部依赖。
后续若要升级为"稠密向量 + Chroma"，只需替换 BookRetriever 内部实现，对外接口不变。
"""
import re

from rank_bm25 import BM25Okapi

from config import settings

_CJK = re.compile(r"[一-鿿]")
_TOKEN = re.compile(r"[a-zA-Z0-9]+")


def tokenize(text: str):
    """极简分词：英文按词、中文按字（demo 足够；生产可换 jieba）。"""
    if not text:
        return []
    text = str(text)
    tokens = [w.lower() for w in _TOKEN.findall(text)]
    tokens += [c for c in text if _CJK.match(c)]
    return tokens


class BookRetriever:
    def __init__(self):
        self.books = []
        self.corpus = []
        self.bm25 = None

    def build(self, books):
        self.books = books or []
        self.corpus = [tokenize(self._doc_text(b)) for b in self.books]
        self.bm25 = BM25Okapi(self.corpus) if self.corpus else None

    @staticmethod
    def _doc_text(b) -> str:
        parts = [
            str(b.get("title") or ""),
            str(b.get("author") or ""),
            str(b.get("description") or ""),
            str(b.get("condition_level") or ""),
        ]
        return " ".join(p for p in parts if p)

    def search(self, query: str, top_k: int = None):
        top_k = top_k or settings.TOP_K
        if not self.bm25:
            return []
        scores = self.bm25.get_scores(tokenize(query))
        ranked = sorted(range(len(scores)), key=lambda i: scores[i], reverse=True)[:top_k]
        return [self.books[i] for i in ranked if scores[i] > 0]
