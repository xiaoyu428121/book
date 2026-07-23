import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000 // 增加超时时间到15秒
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    console.log(`[请求] ${config.method?.toUpperCase()} ${config.url}`, config.data || '')
    return config
  },
  error => {
    console.error('[请求错误]', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    console.log(`[响应] ${response.config.url}`, res)
    
    if (res.code === 200) {
      return res
    } else if (res.code === 401) {
      // 登录过期
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
      return Promise.reject(new Error(res.message || '未登录'))
    } else if (res.code === 403) {
      // 无权限
      ElMessage.error('无权限访问: ' + (res.message || ''))
      return Promise.reject(new Error(res.message || '无权限'))
    } else if (res.code === 404) {
      // 接口不存在
      ElMessage.error('接口不存在: ' + (res.message || ''))
      console.error('[404错误]', response.config.url, res.message)
      return Promise.reject(new Error(res.message || '接口不存在'))
    } else if (res.code === 500) {
      // 服务器错误
      ElNotification.error({
        title: '服务器错误',
        message: res.message || '服务器内部错误，请稍后重试',
        duration: 5000
      })
      console.error('[500服务器错误]', response.config.url, res.message)
      return Promise.reject(new Error(res.message || '服务器错误'))
    } else {
      // 其他业务错误
      ElMessage.error(res.message || '请求失败')
      console.warn('[业务错误]', response.config.url, res.message)
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    // 网络错误或请求被取消
    if (error.response) {
      // 服务器返回了错误状态码
      const { status, data } = error.response
      console.error(`[HTTP错误] ${status}`, data)
      
      switch (status) {
        case 400:
          ElMessage.error('请求参数错误: ' + (data.message || ''))
          break
        case 401:
          ElMessage.error('未登录或登录已过期')
          localStorage.removeItem('token')
          localStorage.removeItem('user')
          router.push('/login')
          break
        case 403:
          ElMessage.error('无权限访问')
          break
        case 404:
          ElMessage.error('接口不存在: ' + (data.message || error.config.url))
          break
        case 500:
          ElNotification.error({
            title: '服务器错误',
            message: data.message || '服务器内部错误，请稍后重试',
            duration: 5000
          })
          break
        case 502:
        case 503:
        case 504:
          ElMessage.error('服务器暂时不可用，请稍后重试')
          break
        default:
          ElMessage.error('请求失败: ' + (data.message || `HTTP ${status}`))
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      console.error('[网络错误] 请求未收到响应', error.config?.url)
      ElMessage.error('网络连接失败，请检查网络后重试')
    } else {
      // 请求配置出错
      console.error('[请求配置错误]', error.message)
      ElMessage.error('请求配置错误: ' + error.message)
    }
    return Promise.reject(error)
  }
)

export default request
