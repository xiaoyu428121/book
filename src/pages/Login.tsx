import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { Mail, Phone, Lock, User, GraduationCap } from 'lucide-react'
import api from '../lib/api'
import { useAuthStore } from '../store/userStore'
import type { AuthResponse } from '../../shared/types'

export default function Login() {
  const [isLogin, setIsLogin] = useState(true)
  const [formData, setFormData] = useState({
    account: '',
    password: '',
    email: '',
    phone: '',
    nickname: ''
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  
  const navigate = useNavigate()
  const { setToken, setUser } = useAuthStore()

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      let response
      if (isLogin) {
        response = await api.post<AuthResponse>('/auth/login', {
          account: formData.account,
          password: formData.password
        })
      } else {
        response = await api.post<AuthResponse>('/auth/register', {
          email: formData.email || undefined,
          phone: formData.phone || undefined,
          password: formData.password,
          nickname: formData.nickname || undefined
        })
      }

      if (response.data.success && response.data.data) {
        setToken(response.data.data.token)
        
        // 获取用户信息
        const userRes = await api.get('/auth/me')
        if (userRes.data.success) {
          setUser(userRes.data.data)
        }

        // 根据是否完成诊断跳转
        if (response.data.data.hasCompletedDiagnostic) {
          navigate('/path')
        } else {
          navigate('/diagnostic')
        }
      }
    } catch (err: any) {
      setError(err.response?.data?.error || '操作失败,请稍后重试')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-cyan-500 via-blue-500 to-teal-500 p-4">
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-5xl bg-white rounded-2xl shadow-2xl overflow-hidden flex flex-col md:flex-row"
      >
        {/* 左侧品牌区 */}
        <div className="w-full md:w-1/2 bg-gradient-to-br from-blue-600 to-teal-500 p-8 md:p-12 text-white flex flex-col justify-center">
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.2 }}
          >
            <div className="flex items-center gap-3 mb-6">
              <GraduationCap size={48} />
              <h1 className="text-3xl font-bold">AI语言学习平台</h1>
            </div>
            <p className="text-lg mb-8 opacity-90">
              智能诊断你的英语水平,个性化生成学习路径,通过AI对话和跟读练习快速提升听说能力
            </p>
            <div className="space-y-4 text-sm opacity-80">
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 bg-white rounded-full"></div>
                <span>AI自适应诊断,精准评估能力</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 bg-white rounded-full"></div>
                <span>实时语音对话,沉浸式练习</span>
              </div>
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 bg-white rounded-full"></div>
                <span>智能跟读打分,纠正发音</span>
              </div>
            </div>
          </motion.div>
        </div>

        {/* 右侧表单区 */}
        <div className="w-full md:w-1/2 p-8 md:p-12">
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            transition={{ delay: 0.3 }}
          >
            <h2 className="text-2xl font-bold text-gray-800 mb-8">
              {isLogin ? '欢迎回来' : '创建账号'}
            </h2>

            {error && (
              <motion.div 
                initial={{ opacity: 0, y: -10 }}
                animate={{ opacity: 1, y: 0 }}
                className="mb-4 p-3 bg-red-50 border border-red-200 text-red-600 rounded-lg text-sm"
              >
                {error}
              </motion.div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              {isLogin ? (
                <>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      邮箱或手机号
                    </label>
                    <div className="relative">
                      <input
                        type="text"
                        value={formData.account}
                        onChange={(e) => setFormData({ ...formData, account: e.target.value })}
                        className="w-full px-4 py-3 pl-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        placeholder="请输入邮箱或手机号"
                        required
                      />
                      <Mail className="absolute left-3 top-3.5 text-gray-400" size={20} />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      密码
                    </label>
                    <div className="relative">
                      <input
                        type="password"
                        value={formData.password}
                        onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        className="w-full px-4 py-3 pl-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        placeholder="请输入密码"
                        required
                        minLength={8}
                      />
                      <Lock className="absolute left-3 top-3.5 text-gray-400" size={20} />
                    </div>
                  </div>
                </>
              ) : (
                <>
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        邮箱(可选)
                      </label>
                      <div className="relative">
                        <input
                          type="email"
                          value={formData.email}
                          onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                          className="w-full px-4 py-3 pl-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                          placeholder="email@example.com"
                        />
                        <Mail className="absolute left-3 top-3.5 text-gray-400" size={20} />
                      </div>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        手机号(可选)
                      </label>
                      <div className="relative">
                        <input
                          type="tel"
                          value={formData.phone}
                          onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                          className="w-full px-4 py-3 pl-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                          placeholder="13800138000"
                        />
                        <Phone className="absolute left-3 top-3.5 text-gray-400" size={20} />
                      </div>
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      昵称
                    </label>
                    <div className="relative">
                      <input
                        type="text"
                        value={formData.nickname}
                        onChange={(e) => setFormData({ ...formData, nickname: e.target.value })}
                        className="w-full px-4 py-3 pl-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        placeholder="请输入昵称"
                      />
                      <User className="absolute left-3 top-3.5 text-gray-400" size={20} />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      密码
                    </label>
                    <div className="relative">
                      <input
                        type="password"
                        value={formData.password}
                        onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                        className="w-full px-4 py-3 pl-10 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                        placeholder="至少8位字符"
                        required
                        minLength={8}
                      />
                      <Lock className="absolute left-3 top-3.5 text-gray-400" size={20} />
                    </div>
                  </div>
                </>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 bg-gradient-to-r from-blue-600 to-teal-500 text-white font-medium rounded-lg hover:shadow-lg transition-all disabled:opacity-50"
              >
                {loading ? '处理中...' : (isLogin ? '登录' : '注册')}
              </button>
            </form>

            <div className="mt-6 text-center text-sm text-gray-600">
              {isLogin ? '还没有账号?' : '已有账号?'}
              <button
                onClick={() => {
                  setIsLogin(!isLogin)
                  setError('')
                }}
                className="ml-1 text-blue-600 hover:underline font-medium"
              >
                {isLogin ? '立即注册' : '立即登录'}
              </button>
            </div>
          </motion.div>
        </div>
      </motion.div>
    </div>
  )
}