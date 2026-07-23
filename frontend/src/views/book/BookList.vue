<template>
  <Layout>
    <div class="book-list-container">
      <!-- 顶部搜索+分类+操作栏 -->
      <div class="search-bar">
        <el-input
          v-model="query.keyword"
          placeholder="搜索教材名称、ISBN..."
          size="large"
          class="search-input"
          @keyup.enter="getBooks"
        >
          <template #append>
            <el-button type="primary" :icon="Search" @click="getBooks" class="search-btn" />
          </template>
        </el-input>
        <!-- 二级分类级联选择器 -->
        <el-cascader
          v-model="query.categoryId"
          :options="categoryTree"
          :props="{ 
            checkStrictly: true,
            emitPath: false,
            label: 'label',
            value: 'value'
          }"
          placeholder="选择分类"
          size="large"
          class="cascader-select"
          clearable
          @change="handleCategoryChange"
        >
          <template #empty>
            <span style="padding: 20px; color: #909399;">加载分类中...</span>
          </template>
        </el-cascader>
        <el-button type="success" size="large" class="publish-btn" @click="goPublish">
          发布教材
        </el-button>
      </div>
      <!-- 教材列表卡片 -->
      <el-card class="book-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">教材列表</span>
            <span class="count">共 {{ total }} 本教材（在售 {{ onSaleCount }} / 已售出 {{ soldCount }}）</span>
          </div>
        </template>
        <div v-loading="loading" class="book-grid">
          <div v-if="!showBooks.length && !loading" class="empty-tip">
            暂无教材，快去发布第一本吧！
          </div>
          <div
            v-for="book in showBooks"
            :key="book.id"
            class="book-item"
            :class="{ 'book-sold': book.status === 2 }"
            @click="goDetail(book.id)"
          >
            <!-- 已售出标记 -->
            <div v-if="book.status === 2" class="sold-tag">已售出</div>
            
            <el-image
              :src="getFirstImage(book.images)"
              fit="cover"
              class="book-cover"
              :preview-src-list="parseImages(book.images)"
            >
              <template #error>
                <div class="cover-placeholder">
                  <el-icon><Reading /></el-icon>
                </div>
              </template>
            </el-image>
            <div class="book-title">{{ book.title }}</div>
            <div class="book-price">¥{{ book.price }}</div>
            <div class="book-meta">
              <span>{{ book.isbn || '无ISBN' }}</span>
              <span>{{ book.conditionLevel || '未知成色' }}</span>
            </div>
          </div>
        </div>
        <div v-if="total > pageSize" class="pagination-wrapper">
          <el-pagination
            :current-page="query.page"
            :total="total"
            :page-size="pageSize"
            @current-change="handlePageChange"
            layout="prev, pager, next"
          />
        </div>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Reading } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { getBookList } from '@/api/book'
import { getCategoryTree } from '@/api/category'

const router = useRouter()
const route = useRoute()
const categoryTree = ref([])
const allBooks = ref([])
const total = ref(0)
const loading = ref(false)
const pageSize = 18

const query = ref({
  page: Number(route.query.page) || 1,
  keyword: route.query.keyword || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : null
})

// 统计数量
const onSaleCount = computed(() => allBooks.value.filter(b => b.status === 1).length)
const soldCount = computed(() => allBooks.value.filter(b => b.status === 2).length)

const showBooks = computed(() => {
  const start = (query.value.page - 1) * pageSize
  const end = start + pageSize
  return allBooks.value.slice(start, end)
})

/**
 * ✅ 终极图片解析函数：兼容所有格式
 */
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

/**
 * ✅ 获取第一张图片
 */
const getFirstImage = (images) => {
  const list = parseImages(images)
  if (!list || !list.length) return ''
  const url = list[0]
  if (url.startsWith('/')) {
    return 'http://localhost:8080' + url
  }
  return url
}

const sortBooks = () => {
  const sortType = route.query.sort
  if (!allBooks.value || !allBooks.value.length || !sortType) return
  
  let sortedList = [...allBooks.value]
  
  switch(sortType) {
    case 'sales':
      sortedList = sortedList.sort((a, b) => (b.tradeCount || 0) - (a.tradeCount || 0)).slice(0, 9)
      break
    case 'new':
      sortedList = sortedList.sort((a, b) => new Date(b.createTime) - new Date(a.createTime)).slice(0, 9)
      break
    case 'cheap':
      sortedList = sortedList.sort((a, b) => (a.price || 0) - (b.price || 0)).slice(0, 9)
      break
  }
  
  allBooks.value = sortedList
  total.value = sortedList.length
}

const loadCategoryTree = async () => {
  try {
    const res = await getCategoryTree()
    categoryTree.value = res.data || []
  } catch (error) {
    console.error('获取分类树失败:', error)
    ElMessage.error('分类数据加载失败，请稍后重试')
  }
}

const handleCategoryChange = () => {
  query.value.page = 1
  router.push({
    query: {
      ...route.query,
      categoryId: query.value.categoryId || undefined,
      page: 1
    }
  })
  getBooks()
}

const getBooks = async () => {
  loading.value = true
  try {
    const params = {
      page: 1,
      size: 1000,
      keyword: query.value.keyword || undefined,
      categoryId: query.value.categoryId || undefined
    }
    const res = await getBookList(params)
    const allList = res.data.records || res.data || []
    
    // ✅ 显示 status=1（在售）和 status=2（已售出）
    const validBooks = allList.filter(book => book.status === 1 || book.status === 2)
    
    // ✅ 排序：在售在前，已售出在后；同状态按时间倒序
    allBooks.value = validBooks.sort((a, b) => {
      if (a.status !== b.status) {
        return a.status - b.status // 1在前，2在后
      }
      return new Date(b.createTime) - new Date(a.createTime)
    })
    
    total.value = allBooks.value.length
    
    sortBooks()
  } catch (error) {
    console.error('获取教材列表失败:', error)
    ElMessage.error('教材数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goDetail = (id) => router.push(`/book/detail/${id}`)
const goPublish = () => router.push('/book/publish')

const handlePageChange = (page) => {
  query.value.page = page
  router.push({
    query: {
      ...route.query,
      page: page
    }
  })
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(() => route.query, (newQuery) => {
  query.value = {
    page: Number(newQuery.page) || 1,
    keyword: newQuery.keyword || '',
    categoryId: newQuery.categoryId ? Number(newQuery.categoryId) : null
  }
  getBooks()
}, { deep: true })

watch(() => route.query.sort, () => {
  sortBooks()
}, { immediate: true })

onMounted(() => {
  loadCategoryTree()
  getBooks()
})
</script>

<style scoped>
.book-list-container {
  width: 100%;
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 16px;
  box-sizing: border-box;
}
.search-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 24px;
}
.search-input {
  flex: 1;
  min-width: 280px;
}
:deep(.search-input .el-input__inner) {
  height: 48px;
  border-radius: 8px 0 0 8px;
  border: 1px solid #e2d5c0;
  background: #fdf9f2;
  font-size: 15px;
}
:deep(.search-input .el-input__inner:focus) {
  border-color: #8c2323;
  box-shadow: 0 0 0 3px rgba(140, 35, 35, 0.1);
}
.search-btn {
  height: 48px;
  background-color: #8c2323;
  border-color: #8c2323;
  border-radius: 0 8px 8px 0;
}
.search-btn:hover {
  background-color: #a02c2c !important;
  border-color: #a02c2c !important;
}
.cascader-select {
  width: 220px;
  flex-shrink: 0;
}
:deep(.cascader-select .el-cascader__inner) {
  height: 48px;
  border-radius: 8px;
  border: 1px solid #e2d5c0;
  background: #fdf9f2;
}
:deep(.cascader-select.is-focus .el-cascader__inner) {
  border-color: #8c2323;
  box-shadow: 0 0 0 3px rgba(140, 35, 35, 0.1);
}
.publish-btn {
  height: 48px;
  flex-shrink: 0;
  border-radius: 8px;
}
.book-card {
  width: 100%;
  min-height: 400px;
  border: 2px solid #8c2323 !important;
  border-radius: 10px !important;
  background-color: #fffdf8 !important;
  box-shadow: 0 4px 12px rgba(140, 35, 35, 0.1);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #8c2323;
  letter-spacing: 1px;
}
.count {
  font-size: 14px;
  color: #703922;
}
.book-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 20px;
  padding: 20px 16px;
}
.empty-tip {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 0;
  color: #909399;
  font-size: 14px;
}
.book-item {
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  background: #fff;
  border: 1px solid #d4af37;
  border-radius: 8px;
  overflow: hidden;
  padding-bottom: 12px;
  position: relative;
}
.book-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 20px rgba(140, 35, 35, 0.15);
  border-color: #8c2323;
}
/* ✅ 已售出书籍样式 */
.book-item.book-sold {
  opacity: 0.6;
  filter: grayscale(50%);
  border-color: #999;
  cursor: not-allowed;
}
.book-item.book-sold:hover {
  transform: none;
  box-shadow: none;
  border-color: #999;
}
/* ✅ 已售出角标 */
.sold-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  background: linear-gradient(135deg, #f56c6c, #c83030);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(200, 48, 48, 0.3);
}
.book-cover {
  width: 100%;
  height: 200px;
  background: #f5f0e3;
}
.cover-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f0e3;
  color: #b88646;
  font-size: 48px;
}
.book-title {
  padding: 10px 12px 4px;
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #333;
}
.book-price {
  padding: 0 12px;
  font-size: 18px;
  font-weight: bold;
  color: #c83030;
  margin: 4px 0;
}
.book-meta {
  padding: 4px 12px;
  font-size: 12px;
  color: #777;
  display: flex;
  justify-content: space-between;
}
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;
  padding-bottom: 16px;
}
@media (max-width: 1200px) {
  .book-grid {
    grid-template-columns: repeat(4, 1fr);
  }
}
@media (max-width: 992px) {
  .book-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 768px) {
  .search-bar {
    flex-direction: column;
    align-items: stretch;
  }
  .cascader-select {
    width: 100%;
  }
  .publish-btn {
    width: 100%;
  }
  .book-list-container {
    padding: 0 8px;
    margin: 16px auto;
  }
  .book-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }
}
@media (max-width: 480px) {
  .book-grid {
    grid-template-columns: 1fr;
  }
}
</style>