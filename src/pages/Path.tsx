import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { 
  BookOpen, MessageCircle, Mic, FileText, 
  Lock, CheckCircle, Clock, ChevronRight,
  TrendingUp, Award
} from 'lucide-react'
import api from '../lib/api'
import type { PathNode, DailyTask } from '../../shared/types'

export default function Path() {
  const [nodes, setNodes] = useState<PathNode[]>([])
  const [dailyTasks, setDailyTasks] = useState<DailyTask[]>([])
  const [progress, setProgress] = useState(0)
  const [currentLevel, setCurrentLevel] = useState('A1')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchPath()
    fetchDailyTasks()
  }, [])

  const fetchPath = async () => {
    try {
      const response = await api.get('/path')
      if (response.data.success) {
        setNodes(response.data.data.nodes)
        setProgress(response.data.data.progress)
        setCurrentLevel(response.data.data.currentLevel)
      }
    } catch (error) {
      console.error('获取学习路径失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchDailyTasks = async () => {
    try {
      const response = await api.get('/path/daily-tasks')
      if (response.data.success) {
        setDailyTasks(response.data.data.tasks)
      }
    } catch (error) {
      console.error('获取今日任务失败:', error)
    }
  }

  const getNodeIcon = (type: string) => {
    switch (type) {
      case 'vocabulary': return <BookOpen className="w-6 h-6" />
      case 'dialogue': return <MessageCircle className="w-6 h-6" />
      case 'shadowing': return <Mic className="w-6 h-6" />
      case 'grammar': return <FileText className="w-6 h-6" />
      default: return <BookOpen className="w-6 h-6" />
    }
  }

  const getStatusIcon = (status: string) => {
    switch (status) {
      case 'completed': return <CheckCircle className="w-5 h-5 text-green-500" />
      case 'available': return null
      default: return <Lock className="w-5 h-5 text-gray-400" />
    }
  }

  const getDifficultyColor = (difficulty: string) => {
    switch (difficulty) {
      case 'easy': return 'bg-green-100 text-green-700'
      case 'medium': return 'bg-yellow-100 text-yellow-700'
      case 'hard': return 'bg-red-100 text-red-700'
      default: return 'bg-gray-100 text-gray-700'
    }
  }

  const getTaskLink = (task: DailyTask) => {
    if (task.type === 'dialogue') return '/practice/dialogue'
    if (task.type === 'shadowing') return '/practice/shadowing'
    return '/path'
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 py-8">
        {/* 顶部统计卡片 */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            className="bg-white rounded-xl shadow-sm p-6"
          >
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-gray-500">当前等级</h3>
              <Award className="w-5 h-5 text-blue-600" />
            </div>
            <p className="text-3xl font-bold text-blue-600">{currentLevel}</p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="bg-white rounded-xl shadow-sm p-6"
          >
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-gray-500">整体进度</h3>
              <TrendingUp className="w-5 h-5 text-teal-500" />
            </div>
            <div className="flex items-end gap-2">
              <p className="text-3xl font-bold text-teal-500">{progress}%</p>
            </div>
            <div className="mt-2 w-full bg-gray-200 rounded-full h-2">
              <div 
                className="bg-teal-500 h-2 rounded-full transition-all"
                style={{ width: `${progress}%` }}
              />
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="bg-white rounded-xl shadow-sm p-6"
          >
            <div className="flex items-center justify-between mb-2">
              <h3 className="text-gray-500">今日任务</h3>
              <Clock className="w-5 h-5 text-orange-500" />
            </div>
            <div className="flex items-end gap-2">
              <p className="text-3xl font-bold text-orange-500">{dailyTasks.length}</p>
              <p className="text-gray-500 mb-1">个任务</p>
            </div>
          </motion.div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 左侧学习路径 */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-xl font-bold text-gray-800 mb-6">学习路径</h2>
              
              <div className="space-y-4">
                {nodes.map((node, index) => (
                  <motion.div
                    key={node.id}
                    initial={{ opacity: 0, x: -20 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: index * 0.05 }}
                    className={`flex items-start gap-4 p-4 rounded-lg border-2 transition-all ${
                      node.status === 'completed' 
                        ? 'bg-green-50 border-green-200'
                        : node.status === 'available'
                        ? 'bg-blue-50 border-blue-200 hover:shadow-md cursor-pointer'
                        : 'bg-gray-50 border-gray-200 opacity-60'
                    }`}
                  >
                    <div className={`w-12 h-12 rounded-full flex items-center justify-center ${
                      node.status === 'completed'
                        ? 'bg-green-500 text-white'
                        : node.status === 'available'
                        ? 'bg-blue-600 text-white'
                        : 'bg-gray-300 text-gray-500'
                    }`}>
                      {node.status === 'completed' ? (
                        <CheckCircle className="w-6 h-6" />
                      ) : (
                        getNodeIcon(node.nodeType)
                      )}
                    </div>

                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <h3 className="font-medium text-gray-800">{node.title}</h3>
                        {getStatusIcon(node.status)}
                      </div>
                      <p className="text-sm text-gray-500 mb-2">{node.description}</p>
                      <div className="flex items-center gap-3 text-xs">
                        <span className={`px-2 py-1 rounded ${getDifficultyColor(node.difficulty)}`}>
                          {node.difficulty === 'easy' ? '简单' : node.difficulty === 'medium' ? '中等' : '困难'}
                        </span>
                        <span className="flex items-center gap-1 text-gray-500">
                          <Clock className="w-3 h-3" />
                          {node.estimatedMinutes}分钟
                        </span>
                      </div>
                    </div>

                    {node.status === 'completed' && node.score && (
                      <div className="text-right">
                        <div className="text-2xl font-bold text-green-600">{node.score}</div>
                        <div className="text-xs text-gray-500">得分</div>
                      </div>
                    )}
                  </motion.div>
                ))}
              </div>
            </div>
          </div>

          {/* 右侧今日任务 */}
          <div>
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-xl font-bold text-gray-800 mb-6">今日推荐</h2>
              
              <div className="space-y-4">
                {dailyTasks.length > 0 ? (
                  dailyTasks.map((task, index) => (
                    <Link
                      key={task.taskId}
                      to={getTaskLink(task)}
                      className="block"
                    >
                      <motion.div
                        initial={{ opacity: 0, y: 10 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ delay: index * 0.1 }}
                        className="p-4 rounded-lg border-2 border-gray-200 hover:border-blue-300 hover:shadow-md transition-all"
                      >
                        <div className="flex items-center gap-3 mb-2">
                          <div className="w-10 h-10 rounded-lg bg-gradient-to-br from-blue-500 to-teal-400 flex items-center justify-center text-white">
                            {getNodeIcon(task.type)}
                          </div>
                          <div className="flex-1">
                            <h3 className="font-medium text-gray-800">{task.title}</h3>
                            <div className="flex items-center gap-2 text-xs text-gray-500">
                              <span>{task.estimatedMinutes}分钟</span>
                              <span className={`px-2 py-0.5 rounded ${getDifficultyColor(task.difficulty)}`}>
                                {task.difficulty === 'easy' ? '简单' : task.difficulty === 'medium' ? '中等' : '困难'}
                              </span>
                            </div>
                          </div>
                          <ChevronRight className="w-5 h-5 text-gray-400" />
                        </div>
                      </motion.div>
                    </Link>
                  ))
                ) : (
                  <div className="text-center py-8 text-gray-500">
                    <CheckCircle className="w-12 h-12 mx-auto mb-2 text-green-500" />
                    <p>今日任务已全部完成!</p>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}