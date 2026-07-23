<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="title">智循书堂 · 账号登录</h2>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            size="large"
            class="input-item"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
            class="input-item"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            立即登录
          </el-button>
        </el-form-item>
        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(form)
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data))
        ElMessage.success('登录成功，正在跳转首页')
        router.push('/')
      } catch (error) {
        ElMessage.error('登录失败，请检查账号密码')
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* 整体页面：全屏居中 + 渐变背景 + 轻微动态质感 */
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 柔和书香渐变背景，替换原浓烈紫蓝 */
  background: linear-gradient(135deg, #f0e9df 0%, #e6d8c4 100%);
  padding: 16px;
  box-sizing: border-box;
}

/* 登录卡片：圆角、阴影、过渡动画 */
.login-card {
  width: 100%;
  max-width: 420px;
  padding: 48px 36px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(140, 35, 35, 0.12);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

/* 卡片悬浮微动效果 */
.login-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(140, 35, 35, 0.18);
}

/* 标题样式 */
.title {
  text-align: center;
  margin: 0 0 36px;
  color: #8c2323;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: 2px;
}

/* 表单整体 */
.login-form {
  width: 100%;
}

/* 输入框统一样式 */
.input-item :deep(.el-input__inner) {
  height: 48px;
  border-radius: 8px;
  border: 1px solid #e2d5c0;
  background: #fdf9f2;
}
.input-item :deep(.el-input__inner:focus) {
  border-color: #8c2323;
  box-shadow: 0 0 0 3px rgba(140, 35, 0, 0.1);
}

/* 登录按钮 */
.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  background-color: #8c2323;
  border-color: #8c2323;
  font-size: 16px;
  transition: all 0.2s;
}
.login-btn:hover {
  background-color: #a02c2c !important;
  border-color: #a02c2c !important;
}

/* 注册跳转链接 */
.register-link {
  text-align: center;
  margin-top: 24px;
  color: #777;
  font-size: 14px;
}
.register-link a {
  color: #8c2323;
  text-decoration: none;
  margin-left: 4px;
}
.register-link a:hover {
  text-decoration: underline;
}

/* ========== 移动端响应式适配 ========== */
@media (max-width: 576px) {
  .login-card {
    padding: 32px 20px;
  }
  .title {
    font-size: 20px;
    margin-bottom: 28px;
  }
  .login-btn {
    height: 44px;
  }
}
</style>