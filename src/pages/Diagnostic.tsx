import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { Mic, ChevronRight, CheckCircle, Loader2 } from 'lucide-react'
import api from '../lib/api'
import type { DiagnosticTask } from '../../shared/types'

export default function Diagnostic() {
  const [tasks, setTasks] = useState<DiagnosticTask[]>([])
  const [currentTaskIndex, setCurrentTaskIndex] = useState(0)
  const [isRecording, setIsRecording] = useState(false)
  const [transcript, setTranscript] = useState('')
  const [answers, setAnswers] = useState<Record<string, string | string[]>>({})
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [completed, setCompleted] = useState(false)
  const [result, setResult] = useState<any>(null)
  
  const navigate = useNavigate()

  useEffect(() => {
    fetchTasks()
  }, [])

  const fetchTasks = async () => {
    try {
      const response = await api.get('/diagnostic/tasks')
      if (response.data.success) {
        if (response.data.data.completed) {
          navigate('/path')
        } else {
          setTasks(response.data.data.tasks)
        }
      }
    } catch (error) {
      console.error('获取诊断任务失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const currentTask = tasks[currentTaskIndex]
  const progress = tasks.length > 0 ? ((currentTaskIndex + 1) / tasks.length) * 100 : 0

  const handleRecord = async () => {
    setIsRecording(true)
    setTranscript('')
    
    // 模拟录音(实际应使用Web Audio API和ASR服务)
    setTimeout(() => {
      const mockTranscript = currentTask?.content.target || 'Hello, nice to meet you.'
      setTranscript(mockTranscript)
      setIsRecording(false)
    }, 3000)
  }

  const handleSubmitTask = async () => {
    setSubmitting(true)
    
    try {
      const response = await api.post('/diagnostic/submit', {
        taskId: currentTask.taskId,
        type: currentTask.type,
        response: currentTask.type === 'speech' 
          ? { transcript }
          : { answers: Object.entries(answers).map(([questionId, answer]) => ({ questionId, answer })) }
      })

      if (response.data.success) {
        if (currentTaskIndex < tasks.length - 1) {
          setCurrentTaskIndex(currentTaskIndex + 1)
          setTranscript('')
          setAnswers({})
        } else {
          // 完成所有任务,获取结果
          await fetchResult()
        }
      }
    } catch (error) {
      console.error('提交任务失败:', error)
    } finally {
      setSubmitting(false)
    }
  }

  const fetchResult = async () => {
    try {
      const response = await api.get('/diagnostic/result')
      if (response.data.success) {
        setResult(response.data.data)
        setCompleted(true)
      }
    } catch (error) {
      console.error('获取诊断结果失败:', error)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
      </div>
    )
  }

  if (completed && result) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-teal-50 p-4 flex items-center justify-center">
        <motion.div
          initial={{ opacity: 0, scale: 0.9 }}
          animate={{ opacity: 1, scale: 1 }}
          className="bg-white rounded-2xl shadow-xl p-8 max-w-2xl w-full"
        >
          <div className="text-center mb-8">
            <motion.div
              initial={{ scale: 0 }}
              animate={{ scale: 1 }}
              transition={{ delay: 0.2, type: 'spring' }}
            >
              <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
            </motion.div>
            <h2 className="text-2xl font-bold text-gray-800">诊断完成!</h2>
            <p className="text-gray-600 mt-2">你的英语水平等级: {result.level}</p>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 gap-4 mb-8">
            {Object.entries(result.profile).map(([key, value]) => (
              <div key={key} className="bg-gray-50 rounded-lg p-4">
                <div className="text-sm text-gray-500 mb-1">{getLabel(key)}</div>
                <div className="text-2xl font-bold text-blue-600">{value as number}</div>
              </div>
            ))}
          </div>

          <div className="bg-blue-50 rounded-lg p-4 mb-6">
            <h3 className="font-medium text-blue-800 mb-2">学习建议</h3>
            <p className="text-blue-600">{result.recommendation}</p>
          </div>

          <button
            onClick={() => navigate('/path')}
            className="w-full py-3 bg-gradient-to-r from-blue-600 to-teal-500 text-white font-medium rounded-lg hover:shadow-lg transition-all"
          >
            查看我的学习路径
          </button>
        </motion.div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-teal-50 p-4">
      <div className="max-w-3xl mx-auto">
        {/* 顶部进度条 */}
        <div className="mb-8">
          <div className="flex justify-between text-sm text-gray-600 mb-2">
            <span>入学诊断</span>
            <span>{currentTaskIndex + 1} / {tasks.length}</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2">
            <motion.div
              className="bg-gradient-to-r from-blue-600 to-teal-500 h-2 rounded-full"
              initial={{ width: 0 }}
              animate={{ width: `${progress}%` }}
            />
          </div>
        </div>

        <AnimatePresence mode="wait">
          {currentTask && (
            <motion.div
              key={currentTask.taskId}
              initial={{ opacity: 0, x: 20 }}
              animate={{ opacity: 1, x: 0 }}
              exit={{ opacity: 0, x: -20 }}
              className="bg-white rounded-2xl shadow-lg p-8"
            >
              <div className="mb-6">
                <div className="inline-block px-3 py-1 bg-blue-100 text-blue-600 rounded-full text-sm mb-4">
                  {currentTask.type === 'speech' ? '语音任务' : '文本任务'}
                </div>
                <h2 className="text-xl font-bold text-gray-800 mb-2">
                  {currentTask.content.prompt}
                </h2>
              </div>

              {currentTask.type === 'speech' && currentTask.content.target && (
                <div className="space-y-6">
                  <div className="bg-gray-50 rounded-lg p-6">
                    <p className="text-lg font-medium text-gray-700">
                      {currentTask.content.target}
                    </p>
                  </div>

                  <div className="flex flex-col items-center">
                    <button
                      onClick={handleRecord}
                      disabled={isRecording}
                      className={`w-24 h-24 rounded-full flex items-center justify-center transition-all ${
                        isRecording 
                          ? 'bg-red-500 animate-pulse' 
                          : 'bg-gradient-to-r from-blue-600 to-teal-500 hover:shadow-lg'
                      }`}
                    >
                      <Mic className="w-10 h-10 text-white" />
                    </button>
                    <p className="mt-4 text-sm text-gray-500">
                      {isRecording ? '正在录音...' : '点击麦克风开始朗读'}
                    </p>
                  </div>

                  {transcript && (
                    <motion.div
                      initial={{ opacity: 0, y: 10 }}
                      animate={{ opacity: 1, y: 0 }}
                      className="bg-green-50 border border-green-200 rounded-lg p-4"
                    >
                      <p className="text-sm text-green-600 mb-1">识别结果:</p>
                      <p className="text-gray-700">{transcript}</p>
                    </motion.div>
                  )}
                </div>
              )}

              {currentTask.type === 'text' && currentTask.content.questions && (
                <div className="space-y-6">
                  {currentTask.content.questions.map((question) => (
                    <div key={question.questionId} className="space-y-2">
                      <p className="font-medium text-gray-700">{question.question}</p>
                      {question.type === 'choice' && question.options && (
                        <div className="grid grid-cols-2 gap-2">
                          {question.options.map((option) => (
                            <button
                              key={option}
                              onClick={() => setAnswers({ ...answers, [question.questionId]: option })}
                              className={`px-4 py-2 rounded-lg border transition-all ${
                                answers[question.questionId] === option
                                  ? 'bg-blue-600 text-white border-blue-600'
                                  : 'bg-white border-gray-300 hover:border-blue-600'
                              }`}
                            >
                              {option}
                            </button>
                          ))}
                        </div>
                      )}
                      {question.type === 'translate' && (
                        <textarea
                          value={answers[question.questionId] as string || ''}
                          onChange={(e) => setAnswers({ ...answers, [question.questionId]: e.target.value })}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                          placeholder="请输入英文翻译"
                          rows={2}
                        />
                      )}
                      {question.type === 'fill' && (
                        <input
                          type="text"
                          value={answers[question.questionId] as string || ''}
                          onChange={(e) => setAnswers({ ...answers, [question.questionId]: e.target.value })}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                          placeholder="请填写答案"
                        />
                      )}
                    </div>
                  ))}
                </div>
              )}

              <div className="mt-8 flex justify-end">
                <button
                  onClick={handleSubmitTask}
                  disabled={submitting || (currentTask.type === 'speech' && !transcript)}
                  className="px-6 py-3 bg-gradient-to-r from-blue-600 to-teal-500 text-white font-medium rounded-lg hover:shadow-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                >
                  {submitting ? (
                    <Loader2 className="w-5 h-5 animate-spin" />
                  ) : (
                    <>
                      {currentTaskIndex < tasks.length - 1 ? '下一题' : '完成诊断'}
                      <ChevronRight className="w-5 h-5" />
                    </>
                  )}
                </button>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  )
}

function getLabel(key: string): string {
  const labels: Record<string, string> = {
    overall: '总分',
    speaking: '口语',
    listening: '听力',
    reading: '阅读',
    writing: '写作',
    vocabulary: '词汇',
    grammar: '语法'
  }
  return labels[key] || key
}