<template>
  <Layout>
    <div class="user-center">
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">个人信息</span>
            <el-button type="primary" class="edit-btn" @click="showEditDialog = true">
              编辑
            </el-button>
          </div>
        </template>
        <div class="user-info">
          <div class="avatar-section">
            <el-avatar :size="80" :src="user.avatar" class="user-avatar">
              {{ user.nickname?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
          <div class="info-section">
            <div class="info-item">
              <span class="label">用户名：</span>
              <span class="value">{{ user.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">昵称：</span>
              <span class="value">{{ user.nickname || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="label">手机号：</span>
              <span class="value">{{ user.phone || '未绑定' }}</span>
            </div>
            <div class="info-item">
              <span class="label">角色：</span>
              <el-tag type="warning" size="small">{{ user.role === 'admin' ? '管理员' : '普通用户' }}</el-tag>
            </div>
          </div>
        </div>

        <!-- ✅ 新增：交易统计区域（删除了余额） -->
        <div class="stats-section">
          <div class="stat-item">
            <div class="stat-number">{{ sellStats.count }}</div>
            <div class="stat-label">卖出教材</div>
          </div>
          <div class="stat-item">
            <div class="stat-number income">¥{{ sellStats.income }}</div>
            <div class="stat-label">总收益</div>
          </div>
          <div class="stat-item">
            <div class="stat-number">{{ buyStats.count }}</div>
            <div class="stat-label">买入教材</div>
          </div>
          <div class="stat-item">
            <div class="stat-number expense">¥{{ buyStats.expense }}</div>
            <div class="stat-label">总花费</div>
          </div>
        </div>
      </el-card>

      <el-card class="password-card">
        <template #header>
          <span class="card-title">修改密码</span>
        </template>
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="100px"
          class="password-form"
        >
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              show-password
              placeholder="请输入旧密码"
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              show-password
              placeholder="请输入新密码"
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              show-password
              placeholder="请确认新密码"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="passwordLoading" class="submit-btn" @click="handleChangePassword">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-dialog v-model="showEditDialog" title="编辑个人信息" width="400px" class="edit-dialog">
        <el-form
          ref="editFormRef"
          :model="editForm"
          :rules="editRules"
          label-width="80px"
          class="edit-form"
        >
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="editForm.phone" placeholder="请输入手机号" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showEditDialog = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" :loading="editLoading" class="save-btn" @click="handleUpdateProfile">
            保存
          </el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { getProfile, updateProfile, updatePassword } from '@/api/user'

const user = ref({
  username: '',
  nickname: '',
  phone: '',
  avatar: '',
  role: ''
})

// ✅ 交易统计数据
const sellStats = ref({
  count: 0,
  income: '0.00'
})
const buyStats = ref({
  count: 0,
  expense: '0.00'
})

const showEditDialog = ref(false)
const editLoading = ref(false)
const passwordLoading = ref(false)

const editFormRef = ref(null)
const passwordFormRef = ref(null)

const editForm = reactive({
  nickname: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validatePhone = (rule, value, callback) => {
  if (value && !/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('手机号格式不正确'))
  } else {
    callback()
  }
}

const editRules = {
  phone: [{ validator: validatePhone, trigger: 'blur' }]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6到20个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const fetchProfile = async () => {
  try {
    const res = await getProfile()
    user.value = res.data
    editForm.nickname = res.data.nickname
    editForm.phone = res.data.phone
    
    // ✅ 这里可以对接后端接口获取交易统计，目前是模拟数据
    // 对接后端后替换成真实数据即可
    sellStats.value = {
      count: res.data.sellCount || 0,
      income: (res.data.sellIncome || 0).toFixed(2)
    }
    buyStats.value = {
      count: res.data.buyCount || 0,
      expense: (res.data.buyExpense || 0).toFixed(2)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const handleUpdateProfile = async () => {
  if (!editFormRef.value) return
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      editLoading.value = true
      try {
        await updateProfile(editForm)
        await fetchProfile()
        localStorage.setItem('user', JSON.stringify(user.value))
        ElMessage.success('修改成功')
        showEditDialog.value = false
      } catch (error) {
        console.error('修改失败:', error)
        ElMessage.error(error.response?.data?.message || '修改失败')
      } finally {
        editLoading.value = false
      }
    }
  })
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await updatePassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        window.location.href = '/login'
      } catch (error) {
        console.error('修改密码失败:', error)
        ElMessage.error(error.response?.data?.message || '修改密码失败')
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.user-center {
  width: 100%;
  max-width: 1000px;
  margin: 24px auto;
  padding: 0 16px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 统一国风卡片样式 */
.info-card, .password-card {
  width: 100%;
  border: 2px solid #8c2323 !important;
  border-radius: 10px !important;
  background-color: #fffdf8 !important;
  box-shadow: 0 4px 12px rgba(140, 35, 35, 0.1);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #8c2323;
  font-family: 'MaShanZheng', cursive;
  letter-spacing: 1px;
}
.edit-btn {
  background-color: #8c2323 !important;
  border-color: #8c2323 !important;
  border-radius: 6px;
}

/* 个人信息区域 */
.user-info {
  display: flex;
  gap: 40px;
  padding: 20px;
  align-items: flex-start;
}
.avatar-section {
  flex-shrink: 0;
}
.user-avatar {
  border: 3px solid #8c2323;
  background-color: #f8f5eb;
  color: #8c2323;
  font-size: 32px;
  font-weight: 600;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.info-item {
  display: flex;
  align-items: center;
  font-size: 15px;
}
.info-item .label {
  width: 80px;
  color: #703922;
  font-weight: 500;
}
.info-item .value {
  color: #333;
}

/* ✅ 交易统计区域 */
.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 20px;
  border-top: 1px dashed #e2d5c0;
  margin-top: 10px;
}
.stat-item {
  text-align: center;
  padding: 16px 8px;
  background: #f8f5eb;
  border-radius: 8px;
  border: 1px solid #e2d5c0;
}
.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #8c2323;
  margin-bottom: 6px;
}
.stat-number.income {
  color: #2e7d32;
}
.stat-number.expense {
  color: #c83030;
}
.stat-label {
  font-size: 14px;
  color: #703922;
}

/* 修改密码表单 */
.password-form {
  padding: 20px;
  max-width: 500px;
}
:deep(.password-form .el-input__wrapper) {
  height: 44px;
  border-radius: 8px;
  border: 1px solid #e2d5c0;
  background-color: #fdf9f2 !important;
}
:deep(.password-form .el-input__wrapper.is-focus) {
  border-color: #8c2323;
  box-shadow: 0 0 0 3px rgba(140, 35, 35, 0.1);
}

.submit-btn {
  height: 44px;
  padding: 0 32px;
  border-radius: 8px;
  background-color: #8c2323 !important;
  border-color: #8c2323 !important;
}
.submit-btn:hover {
  background-color: #a02c2c !important;
  border-color: #a02c2c !important;
}

/* 编辑弹窗样式 */
:deep(.edit-dialog .el-dialog__header) {
  background-color: #fffdf8;
  border-bottom: 1px solid #e2d5c0;
}
:deep(.edit-dialog .el-dialog__title) {
  color: #8c2323;
  font-family: 'MaShanZheng', cursive;
}
:deep(.edit-dialog .el-dialog__body) {
  background-color: #fffdf8;
}
:deep(.edit-form .el-input__wrapper) {
  height: 42px;
  border-radius: 6px;
  border: 1px solid #e2d5c0;
  background-color: #fdf9f2 !important;
}
.cancel-btn {
  border-color: #e2d5c0;
  color: #703922;
}
.save-btn {
  background-color: #8c2323 !important;
  border-color: #8c2323 !important;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .user-info {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 24px;
  }
  .info-item {
    justify-content: center;
  }
  .stats-section {
    grid-template-columns: repeat(2, 1fr);
  }
  .password-form {
    padding: 16px;
  }
}
</style>