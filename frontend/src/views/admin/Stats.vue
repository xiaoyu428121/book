<template>
  <Layout>
    <div class="statistics">
      <el-row :gutter="20">
        <el-col :span="6" :xs="24" :sm="12" :md="12" :lg="6">
          <el-card class="stat-card">
            <div class="stat-icon user-icon">
              <el-icon :size="40"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalUsers }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="24" :sm="12" :md="12" :lg="6">
          <el-card class="stat-card">
            <div class="stat-icon book-icon">
              <el-icon :size="40"><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalBooks }}</div>
              <div class="stat-label">总教材数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="24" :sm="12" :md="12" :lg="6">
          <el-card class="stat-card">
            <div class="stat-icon order-icon">
              <el-icon :size="40"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalOrders }}</div>
              <div class="stat-label">总订单数</div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6" :xs="24" :sm="12" :md="12" :lg="6">
          <el-card class="stat-card">
            <div class="stat-icon money-icon">
              <el-icon :size="40"><Coin /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">¥{{ stats.totalAmount || '0.00' }}</div>
              <div class="stat-label">交易金额</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </Layout>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { User, Reading, ShoppingCart, Coin } from '@element-plus/icons-vue'
import { getStatistics } from '@/api/admin'

const stats = reactive({
  totalUsers: 0,
  totalBooks: 0,
  totalOrders: 0,
  totalAmount: '0.00'
})

const fetchStatistics = async () => {
  try {
    const res = await getStatistics()
    Object.assign(stats, res.data)
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
/* 页面外层容器 全局统一间距与宽度 */
.statistics {
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 16px;
}

/* 统计卡片 沿用全站卡片规范 */
.stat-card {
  display: flex;
  align-items: center;
  padding: 24px 20px;
  border: 2px solid #8c2323 !important;
  border-radius: 10px !important;
  background-color: #fffdf8 !important;
  box-shadow: 0 4px 12px rgba(140, 35, 35, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  margin-bottom: 20px;
}

/* 卡片悬浮动效 */
.stat-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 20px rgba(140, 35, 35, 0.18);
}

/* 圆形图标容器 */
.stat-icon {
  width: 70px;
  height: 70px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
  color: #ffffff;
  flex-shrink: 0;
}

/* 图标背景色 - 统一国风暖色系，区分模块不花哨 */
.user-icon {
  background: linear-gradient(135deg, #a04a3c, #8c2323);
}
.book-icon {
  background: linear-gradient(135deg, #b88646, #a06c32);
}
.order-icon {
  background: linear-gradient(135deg, #9c6c48, #845a36);
}
.money-icon {
  background: linear-gradient(135deg, #c87e40, #b06c30);
}

/* 数据信息区域 */
.stat-info {
  flex: 1;
}

/* 统计数值 加大字号、加粗、主题深色 */
.stat-value {
  font-size: 34px;
  font-weight: 700;
  color: #8c2323;
  margin-bottom: 6px;
  line-height: 1.1;
}

/* 标签文字 浅棕色调 */
.stat-label {
  font-size: 14px;
  color: #703922;
  letter-spacing: 1px;
}

/* 小屏幕额外微调 */
@media (max-width: 768px) {
  .stat-card {
    padding: 20px 16px;
  }
  .stat-value {
    font-size: 28px;
  }
  .stat-icon {
    width: 60px;
    height: 60px;
  }
}
</style>