<template>
  <Layout>
    <div class="book-detail-container">
      <div v-loading="pageLoading" element-loading-text="加载中..." element-loading-background="rgba(255,253,248,0.9)">
        <el-card class="detail-card">
          <template #header>
            <div class="header">
              <h2 class="page-title">教材详情</h2>
              <div class="header-actions">
                <template v-if="isAdmin">
                  <el-button @click="editMode = !editMode" :type="editMode ? 'warning' : 'primary'" size="large">
                    {{ editMode ? '取消编辑' : '编辑书籍' }}
                  </el-button>
                </template>
                <el-button @click="$router.back()" type="default" size="large">返回</el-button>
              </div>
            </div>
          </template>

          <div class="detail-content">
            <div class="book-images">
              <div v-if="book.status === 2" class="detail-sold-tag">已售出</div>
              <el-image
                v-if="parseImages(book.images).length"
                :src="getFirstImage(book.images)"
                fit="cover"
                class="main-image"
                :class="{ 'image-sold': book.status === 2 }"
                @click="openImgPreview"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Reading /></el-icon>
                  </div>
                </template>
              </el-image>
              <div v-else class="image-placeholder">
                <el-icon><Reading /></el-icon>
              </div>
            </div>

            <div class="book-info">
              <template v-if="editMode && isAdmin">
                <el-form :model="editForm" label-width="80px" class="edit-form">
                  <el-form-item label="书籍标题">
                    <el-input v-model="editForm.title" placeholder="请输入书籍标题" />
                  </el-form-item>
                  <el-form-item label="售价">
                    <el-input-number v-model="editForm.price" :min="0" :precision="2" style="width: 100%" />
                  </el-form-item>
                  <el-form-item label="原价">
                    <el-input-number v-model="editForm.originalPrice" :min="0" :precision="2" style="width: 100%" />
                  </el-form-item>
                  <el-form-item label="书籍状态">
                    <el-select v-model="editForm.status" placeholder="请选择状态" style="width: 100%">
                      <el-option label="在售（可购买）" :value="1" />
                      <el-option label="已下架（不可购买）" :value="2" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="ISBN">
                    <el-input v-model="editForm.isbn" placeholder="请输入ISBN" />
                  </el-form-item>
                  <el-form-item label="作者">
                    <el-input v-model="editForm.author" placeholder="请输入作者" />
                  </el-form-item>
                  <el-form-item label="分类">
                    <el-select v-model="editForm.categoryId" placeholder="请选择分类" style="width: 100%">
                      <el-option label="大学教材・核心类" :value="156" />
                      <el-option label="外语・四六级/考研" :value="157" />
                      <el-option label="计算机与电子" :value="158" />
                      <el-option label="理工科" :value="159" />
                      <el-option label="经管・法律" :value="160" />
                      <el-option label="人文社科" :value="161" />
                      <el-option label="艺术・体育" :value="162" />
                      <el-option label="考试资料・考研" :value="163" />
                      <el-option label="其他图书" :value="164" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="成色">
                    <el-select v-model="editForm.conditionLevel" placeholder="请选择成色" style="width: 100%">
                      <el-option label="全新" value="全新" />
                      <el-option label="九成新" value="九成新" />
                      <el-option label="七成新" value="七成新" />
                      <el-option label="五成新" value="五成新" />
                    </el-select>
                  </el-form-item>
                  <el-form-item label="详情描述">
                    <el-input v-model="editForm.description" type="textarea" :rows="4" placeholder="请输入书籍详情描述" />
                  </el-form-item>
                  <el-form-item>
                    <el-button type="primary" @click="handleSave" size="large">保存修改</el-button>
                    <el-button @click="resetEditForm" size="large">重置</el-button>
                    <el-button type="danger" @click="handleDelete" size="large">删除书籍</el-button>
                  </el-form-item>
                </el-form>
              </template>

              <template v-else>
                <h3 class="book-title">{{ book.title || '暂无书籍信息' }}</h3>
                <div class="book-price">¥{{ book.price || 0 }}</div>
                <div class="price-compare" v-if="book.original_price">
                  <span class="original-price">原价 ¥{{ book.original_price }}</span>
                </div>
                <el-divider />
                <div class="info-row">
                  <span class="label">ISBN：</span>
                  <span>{{ book.isbn || '暂无' }}</span>
                </div>
                <div class="info-row">
                  <span class="label">作者：</span>
                  <span>{{ book.author || '暂无' }}</span>
                </div>
                <div class="info-row">
                  <span class="label">分类：</span>
                  <span>{{ categoryName }}</span>
                </div>
                <div class="info-row">
                  <span class="label">成色：</span>
                  <span>{{ book.condition_level || '未知' }}</span>
                </div>
                <el-divider />
                <div class="description">
                  <span class="label">详情描述：</span>
                  <p>{{ book.description || '暂无描述' }}</p>
                </div>
              </template>
            </div>
          </div>

          <div class="action-bar" v-if="!editMode && book.status === 1">
            <el-button type="primary" size="large" class="cart-btn" @click="handleAddToCart">加入暂存架</el-button>
            <el-button size="large" class="buy-btn" @click="buyNow">立即购买</el-button>
          </div>

          <div class="sold-tip" v-if="book.status === 2">
            <el-icon><Warning /></el-icon>
            该书籍已售出，暂不可购买
          </div>
        </el-card>

        <!-- ==================== 终极稳定版图片预览（完全自己写，不用任何Element组件） ==================== -->
        <el-dialog
          v-model="imgDialogVisible"
          width="95%"
          top="2vh"
          :show-footer="false"
          :close-on-click-modal="true"
          append-to-body
          class="img-preview-dialog"
        >
          <div class="preview-box">
            <!-- 左箭头 -->
            <div class="preview-arrow left" @click="prevImg" v-if="previewImgList.length > 1">
              <el-icon><ArrowLeft /></el-icon>
            </div>
            
            <!-- 中间图片（纯原生img，100%稳定） -->
            <img 
              :src="previewImgList[currentPreviewIndex]" 
              class="preview-img" 
              alt="预览图"
            />
            
            <!-- 右箭头 -->
            <div class="preview-arrow right" @click="nextImg" v-if="previewImgList.length > 1">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>
          
          <!-- 底部页码 -->
          <div class="preview-pagination" v-if="previewImgList.length > 1">
            {{ currentPreviewIndex + 1 }} / {{ previewImgList.length }}
          </div>
        </el-dialog>

        <!-- 智能推荐 -->
        <el-card v-if="similarBooks.length > 0" class="recommend-card similar-card">
          <template #header>
            <div class="header">
              <span class="block-title">
                <span class="icon">🤝</span>
                买过此书的人也买了
                <span class="ai-tag">智能推荐</span>
              </span>
            </div>
          </template>
          <div v-loading="similarLoading" element-loading-text="加载中..." class="book-grid">
            <div v-for="item in similarBooks" :key="item.id" class="book-item" @click="handleGoDetail(item)">
              <el-image
                :src="getFirstImage(item.images)"
                fit="cover"
                class="book-cover"
                @click.stop="openRecommendImg(item)"
              >
                <template #error>
                  <div class="cover-placeholder">
                    <el-icon><Reading /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="book-title">{{ item.title }}</div>
              <div class="book-price">¥{{ item.price }}</div>
              <div class="book-meta">
                <span>{{ item.condition_level || '未知成色' }}</span>
              </div>
            </div>
          </div>
        </el-card>
        <el-card v-else-if="!similarLoading" class="recommend-card similar-card">
          <template #header>
            <div class="header">
              <span class="block-title">
                <span class="icon">🤝</span>
                买过此书的人也买了
                <span class="ai-tag">智能推荐</span>
              </span>
            </div>
          </template>
          <div class="empty-recommend">暂无相关推荐书籍</div>
        </el-card>
      </div>
    </div>
  </Layout>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Reading, Warning, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { getBookById, updateBook, deleteBook } from '@/api/book'
import { addToCart } from '@/api/cart'
import { getSimilarBooks } from '@/api/recommend'

const route = useRoute()
const router = useRouter()

const isAdmin = computed(() => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    return user.role === 'admin'
  } catch {
    return false
  }
})

const pageLoading = ref(false)
const book = ref({})
const similarBooks = ref([])
const similarLoading = ref(false)
const editMode = ref(false)
const editForm = ref({})

// 图片预览变量（极简方案）
const imgDialogVisible = ref(false)
const previewImgList = ref([])
const currentPreviewIndex = ref(0)

const BASE_IMG_URL = 'http://localhost:8080'

const categoryName = computed(() => {
  const categoryMap = {
    156: '大学教材・核心类',
    157: '外语・四六级/考研',
    158: '计算机与电子',
    159: '理工科',
    160: '经管・法律',
    161: '人文社科',
    162: '艺术・体育',
    163: '考试资料・考研',
    164: '其他图书'
  }
  return categoryMap[book.value.category_id] || '未分类'
})

// 解析图片
const parseImages = (images) => {
  if (!images) return []
  let list = []
  try {
    list = JSON.parse(String(images))
  } catch {
    list = String(images).split(',').map(item => item.trim())
  }
  return list.filter(url => !!url)
}

// 获取单张图片
const getFirstImage = (images) => {
  const list = parseImages(images)
  if (!list.length) return ''
  const url = list[0]
  return url.startsWith('/') ? BASE_IMG_URL + url : url
}

// 获取全部预览图片
const getFullImageList = (images) => {
  const list = parseImages(images)
  return list.map(url => {
    const u = url.trim()
    return u.startsWith('/') ? BASE_IMG_URL + u : u
  })
}

// 打开预览
const openImgPreview = () => {
  const imgList = getFullImageList(book.value.images)
  if (imgList.length === 0) {
    ElMessage.info('暂无预览图片')
    return
  }
  previewImgList.value = imgList
  currentPreviewIndex.value = 0
  imgDialogVisible.value = true
}

const openRecommendImg = (item) => {
  const imgList = getFullImageList(item.images)
  if (imgList.length === 0) {
    ElMessage.info('该书籍暂无预览图片')
    return
  }
  previewImgList.value = imgList
  currentPreviewIndex.value = 0
  imgDialogVisible.value = true
}

// 上一张/下一张
const prevImg = () => {
  currentPreviewIndex.value = currentPreviewIndex.value === 0 
    ? previewImgList.value.length - 1 
    : currentPreviewIndex.value - 1
}
const nextImg = () => {
  currentPreviewIndex.value = currentPreviewIndex.value === previewImgList.value.length - 1
    ? 0
    : currentPreviewIndex.value + 1
}

const resetEditForm = () => {
  editForm.value = {
    id: book.value.id,
    title: book.value.title,
    price: book.value.price,
    originalPrice: book.value.original_price,
    status: book.value.status,
    isbn: book.value.isbn,
    author: book.value.author,
    categoryId: book.value.category_id,
    conditionLevel: book.value.condition_level,
    description: book.value.description
  }
}

const handleSave = async () => {
  if (!isAdmin.value) {
    ElMessage.error('您没有权限执行此操作')
    return
  }
  try {
    await updateBook(editForm.value.id, editForm.value)
    ElMessage.success('书籍信息修改成功！')
    editMode.value = false
    getDetail()
  } catch {
    ElMessage.error('修改失败，请稍后重试')
  }
}

const handleDelete = async () => {
  if (!isAdmin.value || !book.value.id) return
  try {
    await ElMessageBox.confirm(
      '⚠️ 此操作将永久删除该书籍，所有相关数据将无法恢复！',
      '删除书籍确认',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
        dangerMode: true
      }
    )
    await deleteBook(book.value.id)
    ElMessage.success('书籍删除成功！')
    router.push('/book/list')
  } catch (err) {
    if (err !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const getDetail = async () => {
  pageLoading.value = true
  try {
    const res = await getBookById(route.params.id)
    if (!res.data || !res.data.id) {
      throw new Error()
    }
    book.value = res.data
    resetEditForm()
    loadSimilarBooks()
  } catch {
    ElMessage.error('书籍不存在，即将返回列表')
    setTimeout(() => {
      router.push('/book/list')
    }, 1500)
  } finally {
    pageLoading.value = false
  }
}

const loadSimilarBooks = async () => {
  similarLoading.value = true
  try {
    const currentId = Number(route.params.id)
    if (isNaN(currentId) || currentId <= 0) {
      similarBooks.value = []
      return
    }
    const res = await getSimilarBooks(currentId, 6)
    const rawList = res.data || []
    similarBooks.value = rawList.filter(item => {
      if (!item || !item.id) return false
      const itemId = Number(item.id)
      const itemStatus = Number(item.status)
      return itemId !== currentId && itemStatus === 1
    })
  } catch {
    similarBooks.value = []
  } finally {
    similarLoading.value = false
  }
}

const handleGoDetail = (item) => {
  if (!item || !item.id) return
  router.push(`/book/detail/${item.id}`).finally(() => {
    window.scrollTo(0, 0)
  })
}

const handleAddToCart = async () => {
  try {
    await addToCart({ bookId: route.params.id })
    ElMessage.success('已加入暂存架')
  } catch {
    ElMessage.error('操作失败，请稍后重试')
  }
}

const buyNow = () => {
  router.push(`/order/checkout?bookId=${route.params.id}`)
}

watch(() => route.params.id, (newId) => {
  if (newId) {
    editMode.value = false
    getDetail()
  }
}, { immediate: true })
</script>

<style scoped>
.book-detail-container {
  width: 100%;
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 16px;
  box-sizing: border-box;
}
.detail-card {
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
.header-actions {
  display: flex;
  gap: 12px;
}
.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #8c2323;
  margin: 0;
  letter-spacing: 1px;
}
.detail-content {
  display: flex;
  gap: 40px;
  padding: 24px 16px;
  align-items: flex-start;
}
.book-images {
  flex-shrink: 0;
  position: relative;
}
.main-image {
  width: 300px;
  height: 400px;
  border-radius: 8px;
  border: 1px solid #d4af37;
  transition: transform 0.3s ease;
  cursor: pointer;
}
.main-image:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(140, 35, 35, 0.15);
}
.image-sold {
  opacity: 0.6;
  filter: grayscale(50%);
}
.detail-sold-tag {
  position: absolute;
  top: 15px;
  right: 15px;
  background: linear-gradient(135deg, #f56c6c, #c83030);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  z-index: 10;
}
.image-placeholder {
  width: 300px;
  height: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f0e3;
  border: 1px solid #d4af37;
  border-radius: 8px;
  color: #b88646;
  font-size: 64px;
}
.book-info {
  flex: 1;
  min-width: 0;
}
.book-title {
  font-size: 26px;
  font-weight: 600;
  color: #442222;
  margin: 0 0 12px;
  line-height: 1.4;
}
.book-price {
  font-size: 34px;
  font-weight: 700;
  color: #c83030;
  margin-bottom: 6px;
  display: inline-block;
}
.original-price {
  font-size: 14px;
  color: #777;
  text-decoration: line-through;
}
.info-row {
  display: flex;
  padding: 10px 0;
  font-size: 15px;
}
.info-row .label {
  color: #703922;
  width: 80px;
  flex-shrink: 0;
}
.info-row span:not(.label) {
  color: #333;
}
:deep(.el-divider__text) {
  color: #8c2323;
}
:deep(.el-divider) {
  border-color: #e2d5c0;
}
.description {
  padding: 10px 0;
}
.description .label {
  color: #703922;
  font-weight: 500;
  display: block;
  margin-bottom: 8px;
}
.description p {
  color: #333;
  line-height: 1.8;
  margin: 0;
  white-space: pre-wrap;
}
.action-bar {
  display: flex;
  gap: 16px;
  padding: 24px 16px 0;
  border-top: 1px solid #e2d5c0;
}
.cart-btn {
  background-color: #8c2323;
  border-color: #8c2323;
}
.buy-btn {
  background-color: #c83030;
  border-color: #c83030;
  color: white;
}
.sold-tip {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  margin-top: 20px;
  background: #f5f0e3;
  border-radius: 8px;
  color: #703922;
}
.recommend-card {
  margin-top: 24px;
  border: 2px solid #8c2323 !important;
  border-radius: 10px !important;
  background-color: #fffdf8 !important;
}
.block-title {
  font-size: 18px;
  font-weight: 600;
  color: #8c2323;
}
.ai-tag {
  margin-left: 8px;
  padding: 2px 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border-radius: 10px;
  font-size: 12px;
}
.book-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  padding: 20px 0;
}
.book-item {
  cursor: pointer;
  transition: transform 0.25s ease;
  background: #fff;
  border: 1px solid #d4af37;
  border-radius: 8px;
  overflow: hidden;
  padding-bottom: 12px;
}
.book-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(140, 35, 35, 0.12);
  border-color: #8c2323;
}
.book-cover {
  width: 100%;
  height: 160px;
  background: #f5f0e3;
}
.cover-placeholder {
  width: 100%;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f0e3;
  color: #b88646;
  font-size: 40px;
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
  font-size: 16px;
  font-weight: bold;
  color: #c83030;
  margin: 4px 0;
}
.book-meta {
  padding: 4px 12px;
  font-size: 12px;
  color: #777;
}
.empty-recommend {
  padding: 40px;
  text-align: center;
  color: #703922;
  font-size: 16px;
}

/* ==================== 图片预览核心样式（100%稳定） ==================== */
:deep(.img-preview-dialog .el-dialog__body) {
  padding: 0 !important;
  overflow: hidden;
}
.preview-box {
  width: 100%;
  height: 80vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: #fff;
}
.preview-img {
  max-width: 90%;
  max-height: 80vh;
  width: auto;
  height: auto;
  object-fit: contain;
}
.preview-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 50px;
  height: 50px;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 24px;
  transition: all 0.2s;
  z-index: 10;
}
.preview-arrow:hover {
  background: rgba(0, 0, 0, 0.7);
}
.preview-arrow.left {
  left: 20px;
}
.preview-arrow.right {
  right: 20px;
}
.preview-pagination {
  text-align: center;
  padding: 12px 0;
  color: #666;
  font-size: 14px;
}

@media (max-width: 992px) {
  .detail-content {
    flex-direction: column;
    align-items: center;
  }
  .book-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 768px) {
  .book-detail-container {
    padding: 0 8px;
    margin: 16px auto;
  }
  .action-bar {
    flex-direction: column;
  }
  .book-grid {
    grid-template-columns: 1fr;
  }
}
</style>