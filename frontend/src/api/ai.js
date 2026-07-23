import axios from 'axios'

// AI 微服务独立实例（走 /ai 代理，不经过后端 8080）
const ai = axios.create({
  baseURL: '/ai',
  timeout: 30000
})

export function aiChat(sessionId, message) {
  return ai.post('/api/ai/chat', { session_id: sessionId, message })
}

export function aiRecommend(query, limit = 6) {
  return ai.post('/api/ai/recommend', { query, limit })
}
