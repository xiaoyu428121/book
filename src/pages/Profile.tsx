import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { User, Clock, Target, TrendingUp, LogOut, Settings } from 'lucide-react'
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import api from '../lib/api'
import { useAuthStore } from '../store/userStore'

export default function Profile() {
  const [user, setUser] = useState<any>(null)
  const [statistics, setStatistics] = useState<any>(null)
  const [period, setPeriod] = useState<'week' | 'month' | 'all'>('week')
  const [loading, setLoading] = useState(true)
  
  const navigate = useNavigate()
  const { logout } = useAuthStore()

  useEffect(() => {
    fetchUserData()
    fetchStatistics()
  }, [period])

  const fetchUserData = async () => {
    try {
      const response = await api.get('/auth/me')
      if (response.data.success) {
        setUser(response.data.data)
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }

  const fetchStatistics = async () => {
    try {
      const response = await api.get(`/user/statistics?period=${period}`)
      if (response.data.success) {
        setStatistics(response.data.data)
      }
    } catch (error) {
      console.error('获取统计数据失败:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 p-4">
      <div className="max-w-5xl mx-auto">
        {/* 用户信息卡片 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="bg-white rounded-xl shadow-sm p-6 mb-6"
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-4">
              <div className="w-16 h-16 rounded-full bg-gradient-to-br from-blue-500 to-teal-400 flex items-center justify-center text-white text-2xl font-bold">
                {user?.nickname?.[0] || 'U'}
              </div>
              <div>
                <h2 className="text-xl font-bold text-gray-800">{user?.nickname || '学习者'}</h2>
                <p className="text-sm text-gray-500">{user?.email || user?.phone}</p>
                <div className="flex items-center gap-2 mt-1">
                  <span className="px-2 py-1 bg-blue-100 text-blue-600 rounded text-xs">
                    {user?.currentLevel || 'A1'} 级别
                  </span>
                </div>
              </div>
            </div>
            <button
              onClick={handleLogout}
              className="flex items-center gap-2 px-4 py-2 text-gray-600 hover:text-red-600 transition-colors"
            >
              <LogOut className="w-5 h-5" />
              <span>退出登录</span>
            </button>
          </div>
        </motion.div>

        {/* 统计卡片 */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="bg-white rounded-xl shadow-sm p-6"
          >
            <div className="flex items-center gap-3 mb-2">
              <Clock className="w-5 h-5 text-blue-600" />
              <h3 className="text-gray-500">总学习时长</h3>
            </div>
            <p className="text-3xl font-bold text-gray-800">{statistics?.totalMinutes || 0}</p>
            <p className="text-sm text-gray-400">分钟</p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="bg-white rounded-xl shadow-sm p-6"
          >
            <div className="flex items-center gap-3 mb-2">
              <Target className="w-5 h-5 text-teal-500" />
              <h3 className="text-gray-500">练习次数</h3>
            </div>
            <p className="text-3xl font-bold text-gray-800">{statistics?.totalSessions || 0}</p>
            <p className="text-sm text-gray-400">次</p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.3 }}
            className="bg-white rounded-xl shadow-sm p-6"
          >
            <div className="flex items-center gap-3 mb-2">
              <User className="w-5 h-5 text-orange-500" />
              <h3 className="text-gray-500">对话轮次</h3>
            </div>
            <p className="text-3xl font-bold text-gray-800">{statistics?.totalRounds || 0}</p>
            <p className="text-sm text-gray-400">轮</p>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4 }}
            className="bg-white rounded-xl shadow-sm p-6"
          >
            <div className="flex items-center gap-3 mb-2">
              <TrendingUp className="w-5 h-5 text-green-500" />
              <h3 className="text-gray-500">平均得分</h3>
            </div>
            <p className="text-3xl font-bold text-gray-800">{statistics?.averageScore || 0}</p>
            <p className="text-sm text-gray-400">分</p>
          </motion.div>
        </div>

        {/* 图表区域 */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.5 }}
          className="bg-white rounded-xl shadow-sm p-6 mb-6"
        >
          <div className="flex items-center justify-between mb-6">
            <h3 className="text-lg font-bold text-gray-800">学习趋势</h3>
            <div className="flex gap-2">
              {['week', 'month', 'all'].map((p) => (
                <button
                  key={p}
                  onClick={() => setPeriod(p as any)}
                  className={`px-3 py-1 rounded-lg text-sm transition-all ${
                    period === p
                      ? 'bg-blue-600 text-white'
                      : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                  }`}
                >
                  {p === 'week' ? '本周' : p === 'month' ? '本月' : '全部'}
                </button>
              ))}
            </div>
          </div>

          <div className="h-64">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={statistics?.dailyData || []}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis 
                  dataKey="date" 
                  stroke="#9ca3af"
                  tick={{ fontSize: 12 }}
                  tickFormatter={(value) => value.slice(5)}
                />
                <YAxis stroke="#9ca3af" tick={{ fontSize: 12 }} />
                <Tooltip 
                  contentStyle={{ 
                    backgroundColor: '#fff', 
                    border: '1px solid #e5e7eb',
                    borderRadius: '8px'
                  }}
                />
                <Line 
                  type="monotone" 
                  dataKey="minutes" 
                  stroke="#0ea5e9" 
                  strokeWidth={2}
                  dot={{ fill: '#0ea5e9' }}
                  name="学习时长(分钟)"
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </motion.div>

        {/* 能力趋势 */}
        {statistics?.skillTrends && (
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.6 }}
            className="bg-white rounded-xl shadow-sm p-6"
          >
            <h3 className="text-lg font-bold text-gray-800 mb-6">能力趋势</h3>
            
            <div className="h-64">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={statistics.skillTrends.slice(-7)}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis 
                    dataKey="date" 
                    stroke="#9ca3af"
                    tick={{ fontSize: 12 }}
                    tickFormatter={(value) => value.slice(5)}
                  />
                  <YAxis stroke="#9ca3af" tick={{ fontSize: 12 }} domain={[0, 100]} />
                  <Tooltip />
                  <Line type="monotone" dataKey="speaking" stroke="#0ea5e9" strokeWidth={2} name="口语" />
                  <Line type="monotone" dataKey="listening" stroke="#10b981" strokeWidth={2} name="听力" />
                  <Line type="monotone" dataKey="reading" stroke="#f59e0b" strokeWidth={2} name="阅读" />
                  <Line type="monotone" dataKey="writing" stroke="#ef4444" strokeWidth={2} name="写作" />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </motion.div>
        )}
      </div>
    </div>
  )
}