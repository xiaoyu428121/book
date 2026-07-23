<template>
  <Layout>
    <!-- 整体外层容器：左右分栏 -->
    <div class="page-wrap">
      <!-- 左侧固定分类侧边栏 对标孔网分层特色 -->
      <aside class="left-category">
        <!-- 第一块：图书主分类 -->
        <div class="cat-block main-cat">
          <div class="cat-title-bar">
            <h3 class="cat-title">图书分类</h3>
          </div>
          <ul class="cat-list">
            <li 
              v-for="item in allCategory" 
              :key="item.id" 
              @click="filterByCat(item.id)"
              :class="{active: activeCatId === item.id}"
            >
              <span class="dot">•</span> {{ item.categoryName }}
            </li>
          </ul>
        </div>
        <!-- 第二块：特色推荐模块 ✅ 完善销量榜 -->
        <div class="cat-block sub-block">
          <div class="sub-title">📚 图书榜单专区</div>
          <ul class="sub-list">
            <!-- <li @click="goToRank('sales')">
              <span class="rank-icon">🔥</span> 热门畅销榜
            </li> -->
            <li @click="goToRank('new')">
              <span class="rank-icon">✨</span> 新书上架榜
            </li>
            <li @click="goToRank('cheap')">
              <span class="rank-icon">💰</span> 低价捡漏榜
            </li>
          </ul>
        </div>
        <!-- 第三块：底部装饰模块 -->
        <div class="cat-block shop-block">
          <div class="shop-text">智循书堂 · 二手教材交换平台</div>
        </div>
      </aside>
      <!-- 右侧主内容区域 -->
      <main class="main-content">
        <!-- 顶部通栏搜索区域 -->
        <div class="top-search-wrap">
          <el-card shadow="never" class="search-card">
            <div class="search-bar">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索教材名称、ISBN..."
                size="large"
                @keyup.enter="handleSearch"
              >
                <template #append>
                  <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
                </template>
              </el-input>
            </div>
          </el-card>
        </div>
        <!-- 活动Banner区块 -->
        <div class="banner-block">
          <div class="banner-inner">
            <h2>旧书新知 · 线上淘书活动</h2>
            <p>穿梭于特色旧书店，探索尘封专题书单，在过往智慧中相遇好书</p>
          </div>
        </div>
        <!-- 热门教材推荐 -->
        <el-card shadow="never" class="recommend-card">
          <template #header>
            <div class="header">
              <span class="block-title">热门教材推荐</span>
              <el-link type="primary" @click="goToRank('sales')">进入教材列表</el-link>
            </div>
          </template>
          <div v-loading="hotLoading" class="book-grid">
            <div v-if="!hotBooks.length && !hotLoading" class="empty-tip">
              暂无热门教材
            </div>
            <div
              v-for="book in hotBooks"
              :key="book.id"
              class="book-item"
              @click="goToDetail(book.id)"
            >
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
                <span>{{ book.conditionLevel || '未知成色' }}</span>
              </div>
            </div>
          </div>
        </el-card>
        <!-- 分类教材区块 -->
        <el-card 
          v-for="category in categoryBooks" 
          :key="category.categoryId" 
          shadow="never" 
          class="recommend-card"
        >
          <template #header>
            <div class="header">
              <span class="block-title">{{ category.categoryName }}</span>
              <el-link type="primary" @click="router.push(`/book/list?categoryId=${category.categoryId}`)">
                查看更多
              </el-link>
            </div>
          </template>
          <div v-loading="categoryLoading" class="book-grid">
            <div v-if="!category.books.length && !categoryLoading" class="empty-tip">
              暂无教材
            </div>
            <div
              v-for="book in category.books"
              :key="book.id"
              class="book-item"
              @click="goToDetail(book.id)"
            >
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
                <span>{{ book.conditionLevel || '未知成色' }}</span>
              </div>
            </div>
          </div>
        </el-card>
      </main>
    </div>
  </Layout>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Reading } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { getHotBooks, getBooksByCategory } from '@/api/recommend'

const router = useRouter()
const route = useRoute()
const searchKeyword = ref('')
const hotBooks = ref([])
const categoryBooks = ref([])
const hotLoading = ref(false)
const categoryLoading = ref(false)
const activeCatId = ref(null)
const allCategory = ref([])

/**
 * ✅ 终极图片解析函数：和其他页面保持一致
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
 * ✅ 获取第一张图片（加前缀）
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

const handleSearch = () => {
  if (searchKeyword.value.trim()) {
    router.push(`/book/list?keyword=${encodeURIComponent(searchKeyword.value)}`)
  } else {
    router.push('/book/list')
  }
}

const goToRank = (sortType) => {
  router.push({
    path: '/book/list',
    query: { sort: sortType }
  })
}

const filterByCat = (catId) => {
  activeCatId.value = catId
  router.push({
    path: '/book/list',
    query: { categoryId: catId }
  })
}

const goToDetail = (id) => {
  router.push(`/book/detail/${id}`)
}

const loadHotBooks = async () => {
  hotLoading.value = true
  try {
    const res = await getHotBooks()
    hotBooks.value = res.data || []
  } catch (error) {
    console.error('加载热门教材失败:', error)
  } finally {
    hotLoading.value = false
  }
}

const loadCategoryBooks = async () => {
  categoryLoading.value = true
  try {
    const res = await getBooksByCategory()
    categoryBooks.value = res.data || []
    allCategory.value = categoryBooks.value.map(item => ({
      id: item.categoryId,
      categoryName: item.categoryName
    }))
  } catch (error) {
    console.error('加载分类教材失败:', error)
  } finally {
    categoryLoading.value = false
  }
}

watch(() => route.query.categoryId, (val) => {
  activeCatId.value = val ? Number(val) : null
}, { immediate: true })

onMounted(() => {
  loadHotBooks()
  loadCategoryBooks()
})
</script>

<style scoped>
.page-wrap {
  display: flex;
  max-width: 1400px;
  margin: 24px auto;
  gap: 24px;
  padding: 0;
  width: 100%;
}
/* 左侧分类侧边栏 */
.left-category {
  width: 220px;
  min-width: 220px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  height: calc(100vh - 24px);
  overflow-y: auto;
  background-color: #f8f5eb;
  border: 2px solid #8c2323;
  border-radius: 0 10px 10px 0;
  box-shadow: 3px 0 10px rgba(140, 35, 35, 0.08);
  padding: 0;
}
.left-category::-webkit-scrollbar {
  width: 6px;
}
.left-category::-webkit-scrollbar-thumb {
  background-color: #c9b89b;
  border-radius: 3px;
}
.left-category::-webkit-scrollbar-track {
  background: transparent;
}
.cat-block {
  padding: 14px 12px;
  border-bottom: 1px dashed #d4c8b0;
}
.cat-block:last-child {
  border-bottom: none;
}
.main-cat .cat-title-bar {
  background: #8c2323;
  padding: 8px 10px;
  border-radius: 6px;
  margin-bottom: 12px;
}
.main-cat .cat-title {
  font-size: 17px;
  margin: 0;
  color: #fffdf8;
  font-family: 'MaShanZheng', cursive;
  letter-spacing: 1px;
}
.cat-list li {
  padding: 9px 8px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  border-radius: 5px;
  transition: all 0.22s ease;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 3px;
  display: flex;
  align-items: center;
  gap: 6px;
}
.cat-list li .dot {
  color: #b88646;
}
.cat-list li:hover {
  background: #e9e2d3;
  color: #8c2323;
}
.cat-list li.active {
  background: #e0cdb0;
  color: #8c2323;
  font-weight: 550;
}
.sub-block .sub-title {
  font-size: 15px;
  font-weight: 600;
  color: #6d3817;
  margin-bottom: 10px;
  padding-left: 4px;
  border-left: 3px solid #b88646;
}
.sub-list li {
  padding: 9px 8px;
  font-size: 14px;
  color: #444;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
  margin-bottom: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.sub-list li:hover {
  background: #efe8d7;
  color: #8c2323;
}
.rank-icon {
  font-size: 16px;
}
.shop-block {
  background: linear-gradient(120deg, #e9ddc8, #f3e9d6);
  text-align: center;
  margin: 8px;
  border-radius: 6px;
  padding: 16px 8px;
}
.shop-text {
  font-size: 13px;
  color: #703922;
  line-height: 1.6;
  font-family: 'MaShanZheng', cursive;
  letter-spacing: 1px;
}
.main-content {
  flex: 1;
  padding-left: 24px;
  padding-right: 16px;
}
.top-search-wrap {
  margin-bottom: 20px;
}
.search-card {
  background-color: #f8f5eb !important;
  border: 2px solid #8c2323 !important;
  border-radius: 8px !important;
  box-shadow: inset 0 0 10px rgba(0, 0.05);
  padding: 24px 0;
}
.search-bar {
  max-width: 700px;
  margin: 0 auto;
}
.search-bar .el-input__inner {
  height: 44px;
  font-size: 15px;
  border: 1px solid #d4af37;
}
.search-bar .el-button {
  border-radius: 0 4px 4px 0;
  background-color: #8c2323;
  border-color: #8c2323;
  color: white;
  height: 44px;
}
.banner-block {
  height: 160px;
  background: linear-gradient(135deg, #e8dfcc, #f3eed9);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  border: 2px solid #8c2323;
  position: relative;
  overflow: hidden;
}
.banner-block::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 80%;
  height: 30px;
  background-color: #8c2323;
  border-radius: 0 0 8px;
}
.banner-inner {
  text-align: center;
  position: relative;
  z-index: 1;
}
.banner-inner h2 {
  font-size: 32px;
  color: #8c2323;
  margin: 0 0 10px;
  font-weight: 600;
  font-family: 'MaShanZheng', cursive;
}
.banner-inner p {
  font-size: 15px;
  color: #555;
  margin: 0;
}
.recommend-card {
  margin-bottom: 24px;
  background-color: #fffdf8 !important;
  border: 2px solid #8c2323 !important;
  border-radius: 8px !important;
  box-shadow: inset 0 0 10px rgba(0, 0, 0.05);
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 10px;
}
.block-title {
  font-size: 18px;
  font-weight: 600;
  color: #442222;
  font-family: 'MaShanZheng', cursive;
}
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 24px;
  padding: 16px;
}
.empty-tip {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 0;
  color: #999;
  font-size: 14px;
}
.book-item {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s;
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #d4af37;
  background-color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
}
.book-item:hover {
  transform: translateY(-6px);
  box-shadow: 0 8px 20px rgba(140, 35, 0.15);
  border-color: #8c2323;
}
.book-cover {
  width: 100%;
  height: 200px;
  border-radius: 6px;
  background: #f5f0e3;
  border: 1px solid #d4af37;
  margin-bottom: 10px;
}
.cover-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f0e3;
  border-radius: 6px;
  color: #ccc;
  border: 1px solid #d4af37;
}
.book-title {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #222;
  text-align: center;
  width: 100%;
}
.book-price {
  margin-top: 6px;
  font-size: 17px;
  font-weight: bold;
  color: #c83030;
}
.book-meta {
  margin-top: 6px;
  font-size: 12px;
  color: #777;
  text-align: center;
  width: 100%;
}
/* 移动端适配 */
@media (max-width: 992px) {
  .page-wrap {
    flex-direction: column;
    padding: 0 16px;
  }
  .left-category {
    width: 100%;
    min-width: 100%;
    height: auto;
    position: static;
    border-radius: 8px;
    margin-bottom: 16px;
  }
  .main-content {
    padding-left: 0;
    padding-right: 0;
  }
}
</style>