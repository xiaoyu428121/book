import { useState, useRef, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { Mic, MicOff, Send, Volume2, ArrowLeft, Loader2 } from 'lucide-react'
import api from '../lib/api'

interface Message {
  id: string
  speaker: 'user' | 'ai'
  content: string
  timestamp: Date
}

export default function Dialogue() {
  const [messages, setMessages] = useState<Message[]>([])
  const [isRecording, setIsRecording] = useState(false)
  const [transcript, setTranscript] = useState('')
  const [loading, setLoading] = useState(false)
  const [sessionId, setSessionId] = useState<string>('')
  const [scene, setScene] = useState({ name: '', description: '' })
  
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const navigate = useNavigate()

  useEffect(() => {
    startSession()
  }, [])

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  const startSession = async () => {
    try {
      const response = await api.post('/practice/dialogue/start', {
        sceneId: 'scene-1',
        difficulty: 'easy'
      })
      
      if (response.data.success) {
        setSessionId(response.data.data.sessionId)
        setScene(response.data.data.scene)
        
        // 添加AI开场白
        setMessages([{
          id: '1',
          speaker: 'ai',
          content: response.data.data.greeting,
          timestamp: new Date()
        }])
      }
    } catch (error) {
      console.error('开始会话失败:', error)
    }
  }

  const handleRecord = async () => {
    if (isRecording) {
      // 停止录音并提交
      setIsRecording(false)
      if (transcript.trim()) {
        await sendMessage(transcript)
        setTranscript('')
      }
    } else {
      // 开始录音
      setIsRecording(true)
      // 模拟录音识别(实际应使用Web Audio API和ASR服务)
      setTimeout(() => {
        const mockTranscripts = [
          'Hello, I am learning English.',
          'Nice to meet you!',
          'How are you today?',
          'I want to practice speaking.',
          'Thank you for your help.'
        ]
        setTranscript(mockTranscripts[Math.floor(Math.random() * mockTranscripts.length)])
      }, 2000)
    }
  }

  const sendMessage = async (text: string) => {
    if (!text.trim() || !sessionId) return
    
    setLoading(true)
    
    // 添加用户消息
    const userMessage: Message = {
      id: Date.now().toString(),
      speaker: 'user',
      content: text,
      timestamp: new Date()
    }
    setMessages(prev => [...prev, userMessage])

    try {
      const response = await api.post('/practice/dialogue/message', {
        sessionId,
        transcript: text
      })

      if (response.data.success) {
        // 添加AI回复
        const aiMessage: Message = {
          id: (Date.now() + 1).toString(),
          speaker: 'ai',
          content: response.data.data.aiResponse,
          timestamp: new Date()
        }
        setMessages(prev => [...prev, aiMessage])
      }
    } catch (error) {
      console.error('发送消息失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const endSession = async () => {
    if (!sessionId) return
    
    try {
      await api.post('/practice/dialogue/end', { sessionId })
      navigate('/path')
    } catch (error) {
      console.error('结束会话失败:', error)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-teal-50 flex flex-col">
      {/* 顶部栏 */}
      <div className="bg-white shadow-sm px-4 py-3 flex items-center justify-between">
        <button onClick={() => navigate('/path')} className="flex items-center gap-2 text-gray-600 hover:text-gray-800">
          <ArrowLeft className="w-5 h-5" />
          <span>返回</span>
        </button>
        <div className="text-center">
          <h2 className="font-medium text-gray-800">{scene.name || 'AI对话练习'}</h2>
          <p className="text-xs text-gray-500">{scene.description}</p>
        </div>
        <button 
          onClick={endSession}
          className="px-4 py-2 bg-red-500 text-white text-sm rounded-lg hover:bg-red-600"
        >
          结束练习
        </button>
      </div>

      {/* 对话区域 */}
      <div className="flex-1 overflow-y-auto p-4">
        <div className="max-w-2xl mx-auto space-y-4">
          <AnimatePresence>
            {messages.map((message) => (
              <motion.div
                key={message.id}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                className={`flex ${message.speaker === 'user' ? 'justify-end' : 'justify-start'}`}
              >
                <div className={`max-w-xs md:max-w-md ${
                  message.speaker === 'user'
                    ? 'bg-blue-600 text-white rounded-2xl rounded-br-md'
                    : 'bg-white text-gray-800 rounded-2xl rounded-bl-md shadow'
                } px-4 py-3`}>
                  <p>{message.content}</p>
                  <p className={`text-xs mt-1 ${
                    message.speaker === 'user' ? 'text-blue-200' : 'text-gray-400'
                  }`}>
                    {message.timestamp.toLocaleTimeString()}
                  </p>
                </div>
              </motion.div>
            ))}
          </AnimatePresence>

          {loading && (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="flex justify-start"
            >
              <div className="bg-white rounded-2xl rounded-bl-md shadow px-4 py-3">
                <div className="flex items-center gap-2">
                  <Loader2 className="w-4 h-4 animate-spin text-blue-600" />
                  <span className="text-gray-500 text-sm">AI正在思考...</span>
                </div>
              </div>
            </motion.div>
          )}

          <div ref={messagesEndRef} />
        </div>
      </div>

      {/* 底部输入区 */}
      <div className="bg-white border-t px-4 py-4">
        <div className="max-w-2xl mx-auto">
          {transcript && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              className="mb-4 p-3 bg-gray-50 rounded-lg"
            >
              <p className="text-sm text-gray-600 mb-1">识别结果:</p>
              <p className="text-gray-800">{transcript}</p>
            </motion.div>
          )}

          <div className="flex items-center justify-center gap-4">
            <button
              onClick={handleRecord}
              disabled={loading}
              className={`w-16 h-16 rounded-full flex items-center justify-center transition-all ${
                isRecording
                  ? 'bg-red-500 animate-pulse text-white'
                  : 'bg-gradient-to-r from-blue-600 to-teal-500 text-white hover:shadow-lg'
              }`}
            >
              {isRecording ? <MicOff className="w-8 h-8" /> : <Mic className="w-8 h-8" />}
            </button>

            {transcript && !isRecording && (
              <button
                onClick={() => {
                  sendMessage(transcript)
                  setTranscript('')
                }}
                disabled={loading}
                className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 flex items-center gap-2"
              >
                <Send className="w-5 h-5" />
                发送
              </button>
            )}
          </div>

          <p className="text-center text-sm text-gray-500 mt-4">
            {isRecording ? '点击停止录音' : '点击麦克风开始说话'}
          </p>
        </div>
      </div>
    </div>
  )
}