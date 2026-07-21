import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Mic, Volume2, ArrowLeft, CheckCircle, Loader2 } from 'lucide-react'
import api from '../lib/api'

export default function Shadowing() {
  const [sentence, setSentence] = useState({
    id: 'sentence-1',
    text: 'Hello, nice to meet you.',
    translation: '你好,很高兴见到你。',
    difficulty: 'easy' as const,
    audioUrl: ''
  })
  const [isRecording, setIsRecording] = useState(false)
  const [transcript, setTranscript] = useState('')
  const [result, setResult] = useState<any>(null)
  const [loading, setLoading] = useState(false)
  const [sessionId, setSessionId] = useState('')
  
  const navigate = useNavigate()

  const handleStart = async () => {
    try {
      const response = await api.post('/practice/shadowing/start', {
        sentenceId: 'sentence-1'
      })
      if (response.data.success) {
        setSessionId(response.data.data.sessionId)
        setSentence(response.data.data.sentence)
      }
    } catch (error) {
      console.error('开始练习失败:', error)
    }
  }

  const handlePlay = () => {
    // 播放标准发音(模拟)
    const utterance = new SpeechSynthesisUtterance(sentence.text)
    utterance.lang = 'en-US'
    speechSynthesis.speak(utterance)
  }

  const handleRecord = async () => {
    if (isRecording) {
      // 停止录音并评分
      setIsRecording(false)
      if (transcript.trim()) {
        await submitRecording(transcript)
      }
    } else {
      // 开始录音
      setIsRecording(false)
      setResult(null)
      setTranscript('')
      
      // 模拟录音识别
      setTimeout(() => {
        const variations = [
          'Hello, nice to meet you.',
          'Hello, nice meet you.',
          'Hi, nice to meet you.',
          'Hello, nice to meet you too.'
        ]
        setTranscript(variations[Math.floor(Math.random() * variations.length)])
        setIsRecording(false)
      }, 2000)
    }
  }

  const submitRecording = async (text: string) => {
    setLoading(true)
    
    try {
      const response = await api.post('/practice/shadowing/submit', {
        sentenceId: sentence.id,
        transcript: text
      })

      if (response.data.success) {
        setResult(response.data.data)
      }
    } catch (error) {
      console.error('提交录音失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const getScoreColor = (score: number) => {
    if (score >= 80) return 'text-green-600'
    if (score >= 60) return 'text-yellow-600'
    return 'text-red-600'
  }

  const getScoreGradient = (score: number) => {
    if (score >= 80) return 'from-green-500 to-emerald-400'
    if (score >= 60) return 'from-yellow-500 to-orange-400'
    return 'from-red-500 to-pink-400'
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-teal-50 p-4">
      <div className="max-w-2xl mx-auto">
        {/* 顶部栏 */}
        <div className="mb-8">
          <button 
            onClick={() => navigate('/path')} 
            className="flex items-center gap-2 text-gray-600 hover:text-gray-800 mb-4"
          >
            <ArrowLeft className="w-5 h-5" />
            <span>返回学习路径</span>
          </button>
          <h1 className="text-2xl font-bold text-gray-800">跟读配音练习</h1>
        </div>

        {/* 句子展示区 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-white rounded-xl shadow-lg p-6 mb-6"
        >
          <div className="flex items-center justify-between mb-4">
            <span className={`px-3 py-1 rounded-full text-sm ${
              sentence.difficulty === 'easy' ? 'bg-green-100 text-green-700' :
              sentence.difficulty === 'medium' ? 'bg-yellow-100 text-yellow-700' :
              'bg-red-100 text-red-700'
            }`}>
              {sentence.difficulty === 'easy' ? '简单' : sentence.difficulty === 'medium' ? '中等' : '困难'}
            </span>
          </div>

          <div className="mb-4">
            <p className="text-2xl font-medium text-gray-800 mb-2">{sentence.text}</p>
            <p className="text-gray-500">{sentence.translation}</p>
          </div>

          <button
            onClick={handlePlay}
            className="flex items-center gap-2 px-4 py-2 bg-blue-50 text-blue-600 rounded-lg hover:bg-blue-100 transition-all"
          >
            <Volume2 className="w-5 h-5" />
            <span>播放标准发音</span>
          </button>
        </motion.div>

        {/* 录音区域 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.1 }}
          className="bg-white rounded-xl shadow-lg p-6 mb-6"
        >
          <h2 className="font-medium text-gray-800 mb-4">跟读录音</h2>

          <div className="flex flex-col items-center">
            <button
              onClick={handleRecord}
              disabled={loading}
              className={`w-20 h-20 rounded-full flex items-center justify-center transition-all ${
                isRecording
                  ? 'bg-red-500 animate-pulse text-white'
                  : 'bg-gradient-to-r from-blue-600 to-teal-500 text-white hover:shadow-lg'
              }`}
            >
              {loading ? (
                <Loader2 className="w-8 h-8 animate-spin" />
              ) : (
                <Mic className="w-10 h-10" />
              )}
            </button>
            <p className="mt-4 text-sm text-gray-500">
              {isRecording ? '正在录音...' : '点击麦克风开始跟读'}
            </p>

            {transcript && (
              <motion.div
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                className="mt-6 w-full p-4 bg-gray-50 rounded-lg"
              >
                <p className="text-sm text-gray-500 mb-1">你说:</p>
                <p className="text-gray-800">{transcript}</p>
              </motion.div>
            )}
          </div>
        </motion.div>

        {/* 评分结果 */}
        {result && (
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            animate={{ opacity: 1, scale: 1 }}
            className="bg-white rounded-xl shadow-lg p-6"
          >
            <div className="flex items-center justify-between mb-6">
              <h2 className="font-medium text-gray-800">评分结果</h2>
              <CheckCircle className="w-6 h-6 text-green-500" />
            </div>

            {/* 总分环形进度 */}
            <div className="flex justify-center mb-6">
              <div className="relative w-32 h-32">
                <svg className="w-full h-full transform -rotate-90">
                  <circle
                    cx="64"
                    cy="64"
                    r="56"
                    stroke="#e5e7eb"
                    strokeWidth="8"
                    fill="none"
                  />
                  <circle
                    cx="64"
                    cy="64"
                    r="56"
                    stroke="url(#gradient)"
                    strokeWidth="8"
                    fill="none"
                    strokeDasharray={`${(result.overallScore / 100) * 352} 352`}
                    strokeLinecap="round"
                  />
                  <defs>
                    <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                      <stop offset="0%" className={getScoreColor(result.overallScore)} />
                      <stop offset="100%" className={getScoreColor(result.overallScore)} />
                    </linearGradient>
                  </defs>
                </svg>
                <div className="absolute inset-0 flex items-center justify-center">
                  <div className="text-center">
                    <p className={`text-3xl font-bold ${getScoreColor(result.overallScore)}`}>
                      {result.overallScore}
                    </p>
                    <p className="text-xs text-gray-500">总分</p>
                  </div>
                </div>
              </div>
            </div>

            {/* 详细评分 */}
            <div className="grid grid-cols-3 gap-4 mb-6">
              <div className="bg-gray-50 rounded-lg p-3 text-center">
                <p className="text-2xl font-bold text-blue-600">{result.pronunciation}</p>
                <p className="text-xs text-gray-500">发音准确度</p>
              </div>
              <div className="bg-gray-50 rounded-lg p-3 text-center">
                <p className="text-2xl font-bold text-teal-600">{result.fluency}</p>
                <p className="text-xs text-gray-500">流利度</p>
              </div>
              <div className="bg-gray-50 rounded-lg p-3 text-center">
                <p className="text-2xl font-bold text-orange-600">{result.intonation}</p>
                <p className="text-xs text-gray-500">语调</p>
              </div>
            </div>

            {/* 改进建议 */}
            <div className="bg-blue-50 rounded-lg p-4">
              <h3 className="font-medium text-blue-800 mb-2">改进建议</h3>
              <p className="text-blue-600">{result.suggestion}</p>
            </div>

            {/* 继续练习按钮 */}
            <button
              onClick={() => {
                setResult(null)
                setTranscript('')
              }}
              className="w-full mt-6 py-3 bg-gradient-to-r from-blue-600 to-teal-500 text-white font-medium rounded-lg hover:shadow-lg transition-all"
            >
              继续练习
            </button>
          </motion.div>
        )}
      </div>
    </div>
  )
}