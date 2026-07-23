<template>
  <Layout>
    <div class="order-list">
      <el-card class="order-card">
        <template #header>
          <el-tabs v-model="activeTab" class="order-tabs">
            <el-tab-pane label="我买到的" name="buy" />
            <el-tab-pane label="我卖出的" name="sell" />
          </el-tabs>
        </template>
        <!-- 表格横向滚动容器，解决小屏挤压 -->
        <div class="table-scroll">
          <el-table :data="orderList" v-loading="loading" class="order-table">
            <el-table-column prop="orderNo" label="订单号" width="180" align="center" />
            <el-table-column prop="bookTitle" label="教材" min-width="150" align="center" />
            <el-table-column prop="price" label="金额" width="100" align="center">
              <template #default="{ row }">
                <span class="price">¥{{ row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)" size="small">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="时间" width="160" align="center">
              <template #default="{ row }">
                {{ formatTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right" align="center">
              <template #default="{ row }">
                <template v-if="activeTab === 'buy'">
                  <el-button type="primary" link size="small" v-if="row.status === '待付款'" @click="handlePay(row)">
                    支付
                  </el-button>
                  <el-button type="success" link size="small" v-if="row.status === '已发货'" @click="handleComplete(row.id)">
                    确认收货
                  </el-button>
                  <el-button type="danger" link size="small" v-if="row.status === '待付款'" @click="handleCancel(row.id)">
                    取消
                  </el-button>
                </template>
                <template v-else>
                  <el-button type="primary" link size="small" v-if="row.status === '已付款'" @click="handleShip(row.id)">
                    发货
                  </el-button>
                </template>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-if="orderList.length === 0 && !loading" description="暂无订单" class="empty-tip" />
      </el-card>
    </div>
    <!-- 支付弹窗 -->
    <PaymentModal 
      v-model:visible="payModalVisible" 
      :order-info="currentPayOrder"
      @success="handlePaySuccess"
      @cancel="payModalVisible = false"
    />
  </Layout>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import PaymentModal from './PaymentModal.vue'
import { getMyBuyOrders, getMySellOrders, payOrder, completeOrder, cancelOrder, shipOrder } from '@/api/order'

const activeTab = ref('buy')
const orderList = ref([])
const loading = ref(false)
const payModalVisible = ref(false)
const currentPayOrder = ref({})

const getStatusText = (status) => {
  if (!status) return "未知"
  // 后端直接返回中文文本，不用数字映射
  return status
}

const getStatusType = (status) => {
  const typeMap = {
    '待付款': 'warning',
    '已付款': 'primary',
    '已发货': 'info',
    '已完成': 'success',
    '已取消': 'danger'
  }
  return typeMap[status] || 'info'
}

// 时间格式化
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载订单列表
const loadOrders = async () => {
  loading.value = true
  try {
    let res
    if (activeTab.value === 'buy') {
      res = await getMyBuyOrders()
    } else {
      res = await getMySellOrders()
    }
    orderList.value = res.data || []
  } catch (error) {
    console.error('加载订单失败:', error)
    ElMessage.error('订单数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 打开支付弹窗
const handlePay = (row) => {
  currentPayOrder.value = {
    id: row.id,
    orderNo: row.orderNo,
    goodsName: row.bookTitle,
    amount: row.price
  }
  payModalVisible.value = true
}

// 支付成功回调
const handlePaySuccess = async () => {
  try {
    await payOrder(currentPayOrder.value.id)
    ElMessage.success('支付成功，订单状态已更新')
    loadOrders()
  } catch (error) {
    console.error('支付失败:', error)
    ElMessage.error('支付操作失败，请稍后重试')
  }
}

// 确认收货
const handleComplete = async (id) => {
  try {
    await ElMessageBox.confirm('确定已收到货物吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await completeOrder(id)
    ElMessage.success('确认收货成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
      ElMessage.error('确认收货失败')
    }
  }
}

// 取消订单
const handleCancel = async (id) => {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelOrder(id)
    ElMessage.success('取消成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消失败:', error)
      ElMessage.error('取消订单失败')
    }
  }
}

// 卖家发货
const handleShip = async (id) => {
  try {
    await ElMessageBox.confirm('确定要发货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await shipOrder(id)
    ElMessage.success('发货成功')
    loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('发货失败:', error)
      ElMessage.error('发货操作失败')
    }
  }
}

// 切换标签重新加载订单
watch(activeTab, () => {
  loadOrders()
})

onMounted(() => {
  loadOrders()
})
</script>

<style scoped>
.order-list {
  width: 100%;
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 16px;
  box-sizing: border-box;
}

.order-card {
  width: 100%;
  border: 2px solid #8c2323 !important;
  border-radius: 10px !important;
  background-color: #fffdf8 !important;
  box-shadow: 0 4px 12px rgba(140, 35, 0.1);
}

.order-tabs {
  width: 100%;
}

:deep(.order-tabs .el-tabs__item) {
  font-size: 16px;
  color: #703922;
}

:deep(.order-tabs .el-tabs__item.is-active) {
  color: #8c2323;
  font-weight: 500;
}

:deep(.order-tabs .el-tabs__active-bar) {
  background-color: #8c2323;
  height: 3px;
}

.table-scroll {
  width: 100%;
  overflow-x: auto;
}

.order-table {
  width: 100% !important;
  --el-table-header-bg-color: #f8f5eb;
  --el-table-header-text-color: #8c2323;
  --el-table-row-hover-bg-color: #f3e9d6;
}

.price {
  color: #c83030;
  font-weight: 500;
  font-size: 14px;
}

.empty-tip {
  padding: 40px 0;
}

:deep(.empty-tip .el-empty__description) {
  color: #703922;
  font-size: 14px;
}

@media (max-width: 768px) {
  .order-list {
    margin: 16px auto;
    padding: 0 8px;
  }
}
</style>