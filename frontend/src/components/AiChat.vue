<template>
  <!-- 悬浮入口：始终可见 -->
  <div class="ai-fab" title="AI 找书助手" @click="open = true">🤖</div>

  <el-dialog v-model="open" title="AI 找书助手" width="440px" class="ai-dialog" :close-on-click-modal="false">
    <div class="ai-provider" :class="providerClass" v-if="lastProvider">
      <template v-if="switching">
        <span class="dot pulse"></span>
        <span class="switch-text">⏳ {{ switchText }}</span>
      </template>
      <template v-else>
        <span class="dot"></span>
        <template v-if="providerInfo.offline">离线模式：当前由检索规则回复（未接入大模型）</template>
        <template v-else>由 <b>{{ providerInfo.label }}</b> 提供 · 多模型自动切换</template>
      </template>
    </div>
    <div class="ai-messages" ref="box">
      <div v-if="messages.length === 0" class="ai-empty">
        试试问我：<br />· 有没有便宜的二手《高等数学》？<br />· 推荐几本考研数学教材<br />· 线性代数还有货吗？
      </div>
      <div v-for="(m, i) in messages" :key="i" :class="['ai-msg', m.role]">
        <div class="ai-bubble">{{ m.content }}</div>
        <div v-if="m.sources && m.sources.length" class="ai-sources">
          <div v-for="b in m.sources" :key="b.id" class="ai-card" @click="goBook(b.id)">
            <div class="ai-card-title">《{{ b.title }}》</div>
            <div class="ai-card-meta">{{ b.author }} · {{ b.price }}元 · {{ b.condition_level || '品相未填' }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="ai-input">
      <el-input
        v-model="input"
        placeholder="例如：有没有便宜的二手高数教材？"
        :disabled="loading"
        @keyup.enter="send"
      />
      <el-button type="primary" :loading="loading" @click="send">发送</el-button>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { aiChat } from '@/api/ai'

const router = useRouter()
const open = ref(false)
const input = ref('')
const loading = ref(false)
const messages = ref([])
const box = ref(null)
// 当前实际服务方（来自 /api/ai/chat 的 provider 字段）
const lastProvider = ref('')
// 本次请求实际尝试过的平台链（顺序，来自 provider_chain）
const lastChain = ref([])
// 切换动画进行中（首平台失败、自动切到备用时短暂展示）
const switching = ref(false)
let switchTimer = null

// provider -> 中文展示 & 是否离线（none/fallback 表示未接入大模型，纯检索规则回复）
const PROVIDER_MAP = {
  deepseek:   { label: 'DeepSeek', offline: false },
  zhipu:      { label: '智谱 GLM', offline: false },
  dashscope:   { label: '通义千问', offline: false },
  siliconflow: { label: '硅基流动', offline: false },
  none:     { label: '', offline: true },
  fallback: { label: '', offline: true },
}
const labelOf = (name) => (PROVIDER_MAP[name] || { label: name, offline: false }).label

const providerInfo = computed(() => PROVIDER_MAP[lastProvider.value] || { label: lastProvider.value, offline: false })

// 切换中状态：首平台失败自动切备用，展示过渡文案
const switchText = computed(() => {
  if (!switching.value || lastChain.value.length < 2) return ''
  const from = labelOf(lastChain.value[0])
  const to = providerInfo.value.label || lastProvider.value
  return `${from} 不可用，自动切换至 ${to}`
})

const providerClass = computed(() => ({
  offline: providerInfo.value.offline,
  switching: switching.value,
}))

// 拿到后端响应后，决定角标如何呈现：多平台尝试则先播"切换中"再定格
function showProvider(data) {
  lastProvider.value = data.provider || ''
  lastChain.value = data.provider_chain || []
  if (switchTimer) { clearTimeout(switchTimer); switchTimer = null }
  if (lastChain.value.length > 1) {
    switching.value = true
    switchTimer = setTimeout(() => { switching.value = false; switchTimer = null }, 1100)
  } else {
    switching.value = false
  }
}

// 每个页面会话一个 sessionId，服务端据此维护多轮上下文
const sessionId = 'u' + Math.random().toString(36).slice(2)

function scrollBottom() {
  nextTick(() => {
    if (box.value) box.value.scrollTop = box.value.scrollHeight
  })
}

async function send() {
  const text = input.value.trim()
  if (!text || loading.value) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  loading.value = true
  scrollBottom()
  try {
    const res = await aiChat(sessionId, text)
    const data = res.data
    showProvider(data)
    messages.value.push({ role: 'assistant', content: data.reply, sources: data.sources || [] })
  } catch (e) {
    ElMessage.error('AI 服务暂不可用，请确认 ai-service 已启动（端口 8000）')
  } finally {
    loading.value = false
    scrollBottom()
  }
}

function goBook(id) {
  open.value = false
  router.push(`/book/detail/${id}`)
}
</script>

<style scoped>
.ai-fab {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: #1f5fbf;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  cursor: pointer;
  box-shadow: 0 6px 18px rgba(31, 95, 191, 0.4);
  z-index: 2000;
  transition: transform 0.2s ease;
}
.ai-fab:hover { transform: scale(1.08); }

.ai-messages { max-height: 380px; overflow-y: auto; padding: 4px; }
.ai-provider {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #3f7a4f;
  background: #eef8f1;
  border: 1px solid #d3ecdb;
  border-radius: 20px;
  padding: 4px 12px;
  margin-bottom: 10px;
  width: fit-content;
  line-height: 1.4;
}
.ai-provider b { color: #1f7a3f; }
.ai-provider .dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #18b56a;
  box-shadow: 0 0 0 3px rgba(24, 181, 106, 0.18);
}
.ai-provider.offline {
  color: #9a7212;
  background: #fbf6e6;
  border-color: #f0e4bf;
}
.ai-provider.offline .dot {
  background: #d99a0e;
  box-shadow: 0 0 0 3px rgba(217, 154, 14, 0.18);
}
/* 切换中：蓝色过渡态 + 脉冲点 */
.ai-provider.switching {
  color: #1f5fbf;
  background: #eaf2fd;
  border-color: #cfe0fb;
}
.ai-provider.switching .dot {
  background: #1f5fbf;
  box-shadow: 0 0 0 3px rgba(31, 95, 191, 0.18);
  animation: ai-dot-pulse 1s ease-in-out infinite;
}
.ai-provider.switching .switch-text { font-weight: 500; }
@keyframes ai-dot-pulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.5); opacity: 0.5; }
}
/* 角标切换时的淡入，让"切换中→定格"更顺滑 */
.ai-provider { animation: ai-provider-in 0.25s ease both; }
@keyframes ai-provider-in {
  from { opacity: 0; transform: translateY(-3px); }
  to { opacity: 1; transform: translateY(0); }
}
.ai-empty { color: #8a94a3; font-size: 13px; line-height: 1.9; padding: 8px 4px; }
.ai-msg { margin-bottom: 12px; }
.ai-msg.user { text-align: right; }
.ai-bubble {
  display: inline-block;
  padding: 8px 12px;
  border-radius: 10px;
  max-width: 86%;
  white-space: pre-wrap;
  line-height: 1.55;
  word-break: break-word;
}
.ai-msg.user .ai-bubble { background: #1f5fbf; color: #fff; }
.ai-msg.assistant .ai-bubble { background: #f0f4fa; color: #1f2733; }
.ai-sources { margin-top: 6px; display: flex; flex-direction: column; gap: 6px; }
.ai-card {
  background: #f7f9fc;
  border: 1px solid #e3e9f2;
  border-radius: 8px;
  padding: 6px 10px;
  cursor: pointer;
}
.ai-card:hover { border-color: #1f5fbf; }
.ai-card-title { font-weight: 600; color: #15396e; }
.ai-card-meta { font-size: 12px; color: #5b6675; }
.ai-input { display: flex; gap: 8px; margin-top: 12px; }
</style>
