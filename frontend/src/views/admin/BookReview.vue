<template>
  <Layout>
    <div class="review-container">
      <el-card class="review-card">
        <template #header>
          <div class="header">
            <h2 class="page-title">教材审核</h2>
            <div class="stats">
              <span class="pending-count">待审核：{{ pendingCount }} 本</span>
            </div>
          </div>
        </template>
        
        <el-table 
          :data="tableData" 
          v-loading="loading"
          class="review-table"
          stripe
        >
          <el-table-column prop="id" label="ID" width="80" align="center" />
          <el-table-column prop="title" label="书名" min-width="180" show-overflow-tooltip />
          <el-table-column prop="author" label="作者" width="100" align="center" />
          
          <!-- 分类列：作者后面、售价前面 -->
          <el-table-column prop="category_id" label="分类" width="140" align="center">
            <template #default="{ row }">
              <span>{{ getCategoryName(row.category_id) }}</span>
            </template>
          </el-table-column>
          
          <el-table-column prop="price" label="售价" width="90" align="center">
            <template #default="{ row }">
              <span class="price">¥{{ row.price }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="condition_level" label="成色" width="90" align="center" />
          <el-table-column prop="images" label="封面" width="100" align="center">
            <template #default="{ row }">
              <el-image
                v-if="getFirstImage(row.images)"
                :src="getFirstImage(row.images)"
                fit="cover"
                class="table-image"
                :preview-src-list="[getFirstImage(row.images)]"
                preview-teleported="true"
              >
                <template #error>
                  <div class="img-placeholder">无图</div>
                </template>
              </el-image>
              <span v-else class="img-placeholder">无图</span>
            </template>
          </el-table-column>
          <el-table-column prop="seller_id" label="卖家ID" width="90" align="center" />
          <el-table-column prop="create_time" label="提交时间" width="160" align="center" />
          
          <!-- 移除详情按钮，只剩通过和拒绝 -->
          <el-table-column label="操作" width="140" align="center" fixed="right">
            <template #default="{ row }">
              <el-button 
                type="success" 
                size="small" 
                @click="handleApprove(row.id)"
              >
                通过
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="handleReject(row.id)"
              >
                拒绝
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <el-empty v-if="!loading && tableData.length === 0" description="暂无待审核教材" />
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import request from '@/utils/request'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const pendingCount = ref(0)

const categoryMap = {
  156: '大学教材・核心类',
  157: '外语・四六级/考研',
  158: '计算机与电子',
  159: '理工科',
  160: '经管・法律',
  161: '人文社科',
  162: '艺术・体育',
  163: '考试资料・考研考公',
  164: '其他图书'
}

const getCategoryName = (categoryId) => {
  return categoryMap[categoryId] || '未分类'
}

const parseImages = (images) => {
  try {
    if (!images) return []
    if (Array.isArray(images)) return images
    const trimmed = String(images).trim()
    if (!trimmed) return []
    try {
      const parsed = JSON.parse(trimmed)
      if (Array.isArray(parsed)) return parsed
      if (typeof parsed === 'string') return [parsed]
    } catch (e) {}
    const urls = trimmed.split(',').map(url => url.trim()).filter(url => url)
    return urls
  } catch {
    return []
  }
}

const getFirstImage = (images) => {
  const list = parseImages(images)
  if (!list || !list.length) return ''
  const url = list[0]
  if (url.startsWith('/')) {
    return 'http://localhost:8080' + url
  }
  return url
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/books/review')
    tableData.value = res.data.records || []
    pendingCount.value = tableData.value.length
  } catch (error) {
    console.error('加载待审核列表失败:', error)
    ElMessage.error('加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleApprove = async (id) => {
  try {
    await ElMessageBox.confirm('确认通过该教材审核？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'success'
    })
    await request.put(`/admin/books/${id}/approve`)
    ElMessage.success('审核通过，教材已上架')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败，请稍后重试')
    }
  }
}

const handleReject = async (id) => {
  try {
    await ElMessageBox.confirm('确认拒绝该教材？', '提示', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await request.put(`/admin/books/${id}/reject`)
    ElMessage.success('已拒绝')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败，请稍后重试')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.review-container {
  width: 100%;
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 16px;
  box-sizing: border-box;
}
.review-card {
  width: 100%;
  border: 2px solid #8c2323 !important;
  border-radius: 10px !important;
  background-color: #fffdf8 !important;
  box-shadow: 0 4px 12px rgba(140, 35, 35, 0.1);
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #8c2323;
  margin: 0;
  letter-spacing: 1px;
  font-family: 'MaShanZheng', cursive;
}
.pending-count {
  color: #c83030;
  font-weight: 500;
  font-size: 14px;
}
.review-table {
  margin-top: 10px;
}
:deep(.el-table th) {
  background-color: #f8f5eb !important;
  color: #8c2323 !important;
  font-weight: 600;
}
:deep(.el-table tr:hover > td) {
  background-color: #fdf9f2 !important;
}
.table-image {
  width: 60px;
  height: 80px;
  border-radius: 4px;
  border: 1px solid #e2d5c0;
  cursor: zoom-in; /* 增加放大光标提示 */
}
.img-placeholder {
  display: inline-block;
  width: 60px;
  height: 80px;
  line-height: 80px;
  text-align: center;
  background: #f5f0e3;
  border-radius: 4px;
  color: #999;
  font-size: 12px;
}
.price {
  color: #c83030;
  font-weight: 600;
}
@media (max-width: 768px) {
  .review-container {
    padding: 0 8px;
    margin: 16px auto;
  }
}

/* 图片预览样式优化 - 仅保留放大预览，移除所有操作按钮 */
:deep(.el-image-viewer) {
  /* 确保预览器层级正常 */
  z-index: 2000 !important;
}
/* 隐藏所有预览器操作按钮（关闭、翻页、下载等） */
:deep(.el-image-viewer__actions) {
  display: none !important;
}
/* 隐藏预览器的指示器 */
:deep(.el-image-viewer__indicator) {
  display: none !important;
}
/* 点击遮罩层或图片区域都可关闭预览 */
:deep(.el-image-viewer__mask),
:deep(.el-image-viewer__img) {
  cursor: zoom-out !important; /* 缩小光标提示 */
}
/* 确保预览图片居中且自适应 */
:deep(.el-image-viewer__img-container) {
  display: flex;
  align-items: center;
  justify-content: center;
}
:deep(.el-image-viewer__img) {
  max-width: 90vw !important;
  max-height: 90vh !important;
  object-fit: contain;
}
</style>