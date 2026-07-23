<template>
  <Layout>
    <div class="order-manage">
      <el-card class="order-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">订单管理</span>
          </div>
        </template>
        <el-table 
          :data="orderList" 
          v-loading="loading" 
          stripe 
          class="order-table"
        >
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="orderNo" label="订单编号" width="180" align="center" />
          <el-table-column prop="bookTitle" label="教材名称" min-width="150" align="center" />
          <el-table-column prop="price" label="交易金额" width="100" align="center">
            <template #default="{ row }">
              <span class="price-text">¥{{ row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="buyerId" label="买家ID" width="80" align="center" />
          <el-table-column prop="sellerId" label="卖家ID" width="80" align="center" />
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getStatusType(row.status)" size="small">
                {{ getStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="addressName" label="收货人" width="100" align="center" />
          <el-table-column prop="addressPhone" label="收货电话" width="120" align="center" />
          <el-table-column prop="createTime" label="创建时间" width="180" align="center" />
        </el-table>

        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchOrderList"
            @current-change="fetchOrderList"
          />
        </div>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { getOrderList } from '@/api/admin'

const loading = ref(false)
const orderList = ref([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 订单状态映射（保留原有逻辑）
const statusMap = {
  0: { text: '待付款', type: 'warning' },
  1: { text: '已付款', type: 'primary' },
  2: { text: '已发货', type: 'info' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'danger' }
}

const getStatusText = (status) => {
  return statusMap[status]?.text || '未知'
}

const getStatusType = (status) => {
  return statusMap[status]?.type || 'info'
}

// 获取订单列表
const fetchOrderList = async () => {
  loading.value = true
  try {
    const res = await getOrderList({
      page: pagination.page,
      size: pagination.size
    })
    orderList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取订单列表失败:', error)
    ElMessage.error('数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOrderList()
})
</script>

<style scoped>
/* 页面外层容器 */
.order-manage {
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 16px;
}

/* 卡片通用样式 全站统一 */
.order-card {
  border: 2px solid #8c2323 !important;
  border-radius: 10px !important;
  background-color: #fffdf8 !important;
  box-shadow: 0 4px 12px rgba(140, 35, 35, 0.1);
}

/* 卡片头部 */
.card-header {
  display: flex;
  align-items: center;
}
.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #8c2323;
  letter-spacing: 1px;
}

/* 表格全局样式 国风配色 */
.order-table {
  --el-table-header-bg-color: #f8f5eb;
  --el-table-header-text-color: #8c2323;
  --el-table-row-hover-bg-color: #f3e9d6;
}

/* 交易金额文字高亮 */
.price-text {
  color: #c83030;
  font-weight: 500;
  font-size: 14px;
}

/* 分页区域 */
.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  padding: 0 10px 10px;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .order-manage {
    margin: 16px auto;
  }
  .pagination {
    justify-content: center;
  }
}
</style>