<template>
  <el-container class="layout-container">
    <el-header class="header">
      <div class="logo" @click="router.push('/')">
        <el-icon><Reading /></el-icon>
        <span>智循书堂</span>
      </div>
      <div class="nav-menu">
        <el-menu
          mode="horizontal"
          :default-active="route.path"
          router
          class="nav-menu-inner"
        >
          <el-menu-item index="/">首页</el-menu-item>
          <el-menu-item index="/book/list">教材列表</el-menu-item>
          <el-menu-item index="/book/publish" v-if="user.token">发布教材</el-menu-item>
          <el-menu-item index="/cart" v-if="user.token">暂存架</el-menu-item>
          <el-menu-item index="/order/list" v-if="user.token">我的订单</el-menu-item>
          <el-menu-item index="/admin/user" v-if="user.token && user.role === 'admin'">用户管理</el-menu-item>
          <el-menu-item index="/admin/book" v-if="user.token && user.role === 'admin'">教材审核</el-menu-item>
          <el-menu-item index="/admin/order" v-if="user.token && user.role === 'admin'">订单管理</el-menu-item>
          <el-menu-item index="/admin/stats" v-if="user.token && user.role === 'admin'">数据统计</el-menu-item>
        </el-menu>
      </div>
      <div class="user-area">
        <template v-if="user.token">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="32" :src="user.avatar">
                {{ user.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ user.nickname || user.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="center">个人中心</el-dropdown-item>
                <el-dropdown-item command="address">地址管理</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" @click="router.push('/login')">登录</el-button>
          <el-button @click="router.push('/register')">注册</el-button>
        </template>
      </div>
    </el-header>
    <el-main class="main">
      <slot></slot>
    </el-main>
    <el-footer class="footer">
      <p>智循书堂 · 高校二手教材循环交易与智能管理系统</p>
    </el-footer>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Reading, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

const user = ref({
  token: '',
  username: '',
  nickname: '',
  avatar: '',
  role: ''
})

onMounted(() => {
  const storedUser = localStorage.getItem('user')
  if (storedUser) {
    user.value = JSON.parse(storedUser)
  }
})

const handleCommand = (command) => {
  if (command === 'center') {
    router.push('/user/center')
  } else if (command === 'address') {
    router.push('/user/address')
  } else if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      user.value = {}
      ElMessage.success('已退出登录')
      router.push('/')
    }).catch(() => {})
  }
}
</script>

<style scoped>
.layout-container {
  min-height: 100vh;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 20px;
  height: 60px;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}

.logo .el-icon {
  font-size: 28px;
  margin-right: 8px;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu-inner {
  border-bottom: none;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
}

.main {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 120px);
}

.footer {
  text-align: center;
  color: #909399;
  font-size: 14px;
  background-color: #fff;
  border-top: 1px solid #ebeef5;
}
</style>
