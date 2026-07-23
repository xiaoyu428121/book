<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="title">智循书堂 · 账号注册</h2>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="0"
        class="register-form"
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
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            size="large"
            show-password
            class="input-item"
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="请输入昵称（选填）"
            prefix-icon="UserFilled"
            size="large"
            class="input-item"
          />
        </el-form-item>
        <el-form-item prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入手机号"
            prefix-icon="Phone"
            size="large"
            class="input-item"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="register-btn"
            :loading="loading"
            @click="handleRegister"
          >
            立即注册
          </el-button>
        </el-form-item>
        <div class="login-link">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/user'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: ''
})

// 密码二次校验
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 手机号校验
const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在3到20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  phone: [
    { validator: validatePhone, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register({
          username: form.username,
          password: form.password,
          nickname: form.nickname,
          phone: form.phone
        })
        ElMessage.success('注册成功，请前往登录')
        router.push('/login')
      } catch (error) {
        ElMessage.error('注册失败，请检查信息或稍后重试')
        console.error('注册失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
/* 整体页面 - 统一书香渐变背景，和登录页风格一致 */
.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0e9df 0%, #e6d8c4 100%);
  padding: 16px;
  box-sizing: border-box;
}

/* 注册卡片 - 圆角、阴影、悬浮动效 */
.register-card {
  width: 100%;
  max-width: 420px;
  padding: 48px 36px;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(140, 35, 35, 0.12);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.register-card:hover {
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
.register-form {
  width: 100%;
}

/* 输入框统一样式 */
.input-item :deep(.el-input__inner) {
  height: 48px;
  border-radius: 8px;
  border: 1px solid #e2d5c0;
  background: #fdf9f2;
  font-size: 15px;
}
.input-item :deep(.el-input__inner:focus) {
  border-color: #8c2323;
  box-shadow: 0 0 0 3px rgba(140, 35, 0, 0.1);
}

/* 注册按钮 - 主题色统一 */
.register-btn {
  width: 100%;
  height: 48px;
  border-radius: 8px;
  background-color: #8c2323;
  border-color: #8c2323;
  font-size: 16px;
  transition: all 0.2s;
}
.register-btn:hover {
  background-color: #a02c2c !important;
  border-color: #a02c2c !important;
}

/* 跳转链接 */
.login-link {
  text-align: center;
  margin-top: 24px;
  color: #777;
  font-size: 14px;
}
.login-link a {
  color: #8c2323;
  text-decoration: none;
  margin-left: 4px;
}
.login-link a:hover {
  text-decoration: underline;
}

/* 移动端适配 */
@media (max-width: 576px) {
  .register-card {
    padding: 32px 20px;
  }
  .title {
    font-size: 20px;
    margin-bottom: 28px;
  }
  .register-btn {
    height: 44px;
  }
}
</style>