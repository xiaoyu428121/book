"""配置中心：所有敏感信息从环境变量 / .env 读取，禁止硬编码。

大模型支持「多提供方自动 fallback」：按优先级装配所有已配置 Key 的
平台（DeepSeek → 智谱 → 硅基流动 → 阿里百炼），调用时若主模型
额度耗尽 / 请求失败，自动切到下一个可用平台，无需人工干预。
"""
import os

from dotenv import load_dotenv, find_dotenv

# 自动向上查找 .env，无论从哪个目录启动都能加载到
load_dotenv(find_dotenv(), override=False)


class Provider:
    """单个大模型提供方（OpenAI 兼容协议）。"""

    def __init__(self, name: str, api_key: str, base_url: str, model: str):
        self.name = name
        self.api_key = api_key
        self.base_url = base_url
        self.model = model

    @property
    def enabled(self) -> bool:
        return bool(self.api_key and self.api_key.strip())


class Settings:
    # ===== 主模型：DeepSeek（OpenAI 兼容）=====
    DEEPSEEK_API_KEY: str = os.getenv("DEEPSEEK_API_KEY", "")
    DEEPSEEK_BASE_URL: str = os.getenv("DEEPSEEK_BASE_URL", "https://api.deepseek.com/v1")
    DEEPSEEK_MODEL: str = os.getenv("DEEPSEEK_MODEL", "deepseek-chat")

    # ===== 备用模型 1：智谱 GLM（glm-4-flash 长期免费）=====
    ZHIPU_API_KEY: str = os.getenv("ZHIPU_API_KEY", "")
    ZHIPU_BASE_URL: str = os.getenv("ZHIPU_BASE_URL", "https://open.bigmodel.cn/api/paas/v4")
    ZHIPU_MODEL: str = os.getenv("ZHIPU_MODEL", "glm-4-flash")

    # ===== 备用模型 2：硅基流动 SiliconFlow（注册送额度）=====
    SILICONFLOW_API_KEY: str = os.getenv("SILICONFLOW_API_KEY", "")
    SILICONFLOW_BASE_URL: str = os.getenv("SILICONFLOW_BASE_URL", "https://api.siliconflow.cn/v1")
    SILICONFLOW_MODEL: str = os.getenv("SILICONFLOW_MODEL", "deepseek-ai/DeepSeek-V3")

    # ===== 备用模型 3：阿里云百炼 DashScope（通义千问）=====
    DASHSCOPE_API_KEY: str = os.getenv("DASHSCOPE_API_KEY", "")
    DASHSCOPE_BASE_URL: str = os.getenv("DASHSCOPE_BASE_URL", "https://dashscope.aliyuncs.com/compatible-mode/v1")
    DASHSCOPE_MODEL: str = os.getenv("DASHSCOPE_MODEL", "qwen-turbo")

    LLM_TEMPERATURE: float = float(os.getenv("LLM_TEMPERATURE", "0.7"))

    # ===== MySQL（书籍语料 + 库存查询）=====
    DB_HOST: str = os.getenv("BOOK_DB_HOST", "localhost")
    DB_PORT: int = int(os.getenv("BOOK_DB_PORT", "3306"))
    DB_USER: str = os.getenv("BOOK_DB_USER", "root")
    DB_PASSWORD: str = os.getenv("BOOK_DB_PASSWORD", "0428")
    DB_NAME: str = os.getenv("BOOK_DB_NAME", "book_cycle")

    # ===== 检索与跨域 =====
    TOP_K: int = int(os.getenv("TOP_K", "5"))
    CORS_ORIGINS: list = os.getenv("CORS_ORIGINS", "*").split(",")

    # ---------- 多提供方装配 ----------
    def _build_providers(self) -> list:
        """按优先级组装可用提供方列表（仅含已配置 Key 的）。"""
        candidates = [
            ("deepseek", self.DEEPSEEK_API_KEY, self.DEEPSEEK_BASE_URL, self.DEEPSEEK_MODEL),
            ("zhipu", self.ZHIPU_API_KEY, self.ZHIPU_BASE_URL, self.ZHIPU_MODEL),
            ("siliconflow", self.SILICONFLOW_API_KEY, self.SILICONFLOW_BASE_URL, self.SILICONFLOW_MODEL),
            ("dashscope", self.DASHSCOPE_API_KEY, self.DASHSCOPE_BASE_URL, self.DASHSCOPE_MODEL),
        ]
        return [Provider(n, k, u, m) for (n, k, u, m) in candidates if k and k.strip()]

    @property
    def providers(self) -> list:
        return self._build_providers()

    @property
    def llm_enabled(self) -> bool:
        """没有任何 Key 时服务仍可启动，仅关闭大模型生成能力。"""
        return len(self.providers) > 0

    @property
    def active_model(self) -> str:
        ps = self.providers
        return ps[0].model if ps else ""

    @property
    def provider_names(self) -> list:
        return [p.name for p in self.providers]


settings = Settings()
