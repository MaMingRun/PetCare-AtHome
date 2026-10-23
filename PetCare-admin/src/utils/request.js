import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(cfg => {
  const t = localStorage.getItem('admin_token')
  if (t) cfg.headers.token = t
  return cfg
})

request.interceptors.response.use(
  resp => {
    const r = resp.data
    // 后端 Result: code=1 成功, code=0 失败
    if (r.code === 1) return r.data
    if (r.code === 401) {
      localStorage.removeItem('admin_token')
      ElMessage.error('登录已过期，请重新登录')
      router.push('/login')
      return Promise.reject(r)
    }
    ElMessage.error(r.msg || '请求失败')
    return Promise.reject(r)
  },
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('admin_token')
      ElMessage.error('登录已过期')
      router.push('/login')
    } else {
      ElMessage.error(err.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default request
