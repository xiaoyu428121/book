<template>
  <Layout>
    <div class="publish-container">
      <el-card class="publish-card">
        <template #header>
          <h2 class="card-title">发布教材</h2>
        </template>
        
        <el-form 
          :model="form" 
          label-width="100px" 
          class="publish-form"
          label-position="left"
        >
          <el-form-item label="书名" required>
            <el-input v-model="form.title" placeholder="请输入书名" class="form-input" />
          </el-form-item>
          
          <el-form-item label="ISBN">
            <el-input v-model="form.isbn" placeholder="请输入ISBN（可选）" class="form-input" />
          </el-form-item>
          
          <el-form-item label="作者">
            <el-input v-model="form.author" placeholder="请输入作者（可选）" class="form-input" />
          </el-form-item>
          
          <el-form-item label="分类">
            <el-select v-model="form.categoryId" placeholder="请选择分类" clearable class="form-select">
              <el-option label="大学教材・核心类" :value="156" />
              <el-option label="外语・四六级/考研" :value="157" />
              <el-option label="计算机与电子" :value="158" />
              <el-option label="理工科" :value="159" />
              <el-option label="经管・法律" :value="160" />
              <el-option label="人文社科" :value="161" />
              <el-option label="艺术・体育" :value="162" />
              <el-option label="考试资料・考研考公" :value="163" />
              <el-option label="其他图书" :value="164" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="原价">
            <el-input v-model="form.originalPrice" placeholder="教材定价（可选）" class="form-input" />
          </el-form-item>
          
          <el-form-item label="售价" required>
            <el-input v-model="form.price" placeholder="您的售价" class="form-input" />
          </el-form-item>
          
          <el-form-item label="成色" required>
            <el-select v-model="form.conditionLevel" class="form-select">
              <el-option label="全新" value="全新" />
              <el-option label="九成新" value="九成新" />
              <el-option label="七成新" value="七成新" />
              <el-option label="五成新" value="五成新" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="教材图片">
            <el-upload
              action="#"
              :http-request="handleImageUpload"
              :before-upload="beforeImageUpload"
              list-type="picture-card"
              :file-list="imageList"
              :on-preview="handlePictureCardPreview"
              :on-remove="handleImageRemove"
              :limit="5"
              accept="image/jpeg,image/png,image/gif,image/webp"
              class="upload-box"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">支持 JPG/PNG/GIF/WebP，最多5张，单张不超过5MB</div>
            <div class="upload-tip default-tip">💡 不上传图片将自动生成「书名+作者」的默认封面</div>
            <el-dialog v-model="previewVisible" class="img-preview-dialog">
              <img :src="previewUrl" alt="预览" style="width: 100%;" />
            </el-dialog>
          </el-form-item>
          
          <!-- <el-form-item label="或图片链接">
            <el-input
              v-model="imageUrlInput"
              placeholder="输入图片URL，多个链接用逗号分隔"
              @blur="handleUrlInput"
              class="form-input"
            >
              <template #append>
                <el-button class="url-add-btn" @click="handleUrlInput">添加</el-button>
              </template>
            </el-input>
            <div class="upload-tip">粘贴网络图片链接，批量添加图片</div>
          </el-form-item> -->
          
          <el-form-item label="描述">
            <el-input 
              v-model="form.description" 
              type="textarea" 
              :rows="4" 
              placeholder="请描述教材的具体情况"
              class="form-textarea"
            />
          </el-form-item>
          
          <el-form-item class="btn-group">
            <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="publish">提交发布</el-button>
            <el-button size="large" class="cancel-btn" @click="$router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { publishBook } from '@/api/book'
import { uploadImage } from '@/api/upload'

const router = useRouter()
const loading = ref(false)
const imageList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')
const imageUrlInput = ref('')

const form = ref({
  title: '',
  isbn: '',
  author: '',
  categoryId: null,
  originalPrice: '',
  price: '',
  conditionLevel: '全新',
  images: '',
  description: ''
})

// ✅ 和数据库分类ID完全一致（156-164）
const categoryColorMap = {
  156: { bg: '#8c2323', text: '#d4af37', name: '核心教材' },
  157: { bg: '#1e3a8a', text: '#93c5fd', name: '外语' },
  158: { bg: '#581c87', text: '#c4b5fd', name: '计算机' },
  159: { bg: '#065f46', text: '#6ee7b7', name: '理工' },
  160: { bg: '#c2410c', text: '#fdba74', name: '经管' },
  161: { bg: '#be185d', text: '#f9a8d4', name: '人文' },
  162: { bg: '#4c1d95', text: '#c4b5fd', name: '艺术' },
  163: { bg: '#a16207', text: '#fde047', name: '考试' },
  164: { bg: '#57534e', text: '#d6d3d1', name: '其他' },
  default: { bg: '#8c2323', text: '#d4af37', name: '通用' }
}

const getCategoryColor = () => {
  const categoryId = form.value.categoryId
  return categoryColorMap[categoryId] || categoryColorMap.default
}

const generateDefaultCover = () => {
  const title = form.value.title || '二手教材'
  const author = form.value.author || '智循书堂'
  const color = getCategoryColor()
  
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="300" height="400">
      <rect fill="${color.bg}" width="300" height="400" rx="8" ry="8" />
      <rect x="10" y="10" width="280" height="380" fill="none" stroke="${color.text}" stroke-width="2" rx="4" ry="4" />
      <text x="150" y="60" font-family="Microsoft YaHei, SimHei, sans-serif" font-size="14" fill="${color.text}" text-anchor="middle">${color.name}类教材</text>
      <text x="150" y="170" font-family="Microsoft YaHei, SimHei, sans-serif" font-size="26" font-weight="bold" fill="white" text-anchor="middle">${title}</text>
      <text x="150" y="230" font-family="Microsoft YaHei, SimHei, sans-serif" font-size="18" fill="${color.text}" text-anchor="middle">${author}</text>
      <text x="150" y="360" font-family="Microsoft YaHei, SimHei, sans-serif" font-size="14" fill="rgba(255,255,255,0.7)" text-anchor="middle">智循书堂 · 循环利用</text>
    </svg>
  `
  
  return 'data:image/svg+xml,' + encodeURIComponent(svg.trim())
}

const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleImageUpload = async ({ file }) => {
  try {
    const res = await uploadImage(file)
    if (res.data) {
      if (form.value.images) {
        const images = JSON.parse(form.value.images)
        if (Array.isArray(images)) {
          images.push(res.data)
          form.value.images = JSON.stringify(images)
        }
      } else {
        form.value.images = JSON.stringify([res.data])
      }
      imageList.value.push({
        url: res.data,
        name: file.name
      })
      ElMessage.success('图片上传成功')
    }
  } catch (err) {
    console.error('上传失败', err)
    ElMessage.error('图片上传失败')
  }
}

const handlePictureCardPreview = (file) => {
  previewUrl.value = file.url
  previewVisible.value = true
}

const handleImageRemove = (file) => {
  const url = file.url
  if (form.value.images) {
    try {
      const images = JSON.parse(form.value.images)
      if (Array.isArray(images)) {
        const index = images.indexOf(url)
        if (index > -1) {
          images.splice(index, 1)
          form.value.images = JSON.stringify(images)
        }
      }
    } catch {
      const urls = form.value.images.split(',')
      const filtered = urls.filter(u => u.trim() !== url)
      form.value.images = filtered.join(',')
    }
  }
  const idx = imageList.value.findIndex(item => item.url === url)
  if (idx > -1) {
    imageList.value.splice(idx, 1)
  }
}

const handleUrlInput = () => {
  const url = imageUrlInput.value.trim()
  if (!url) return
  const urls = url.split(',').map(u => u.trim()).filter(u => u)
  let currentImages = []
  if (form.value.images) {
    try {
      currentImages = JSON.parse(form.value.images)
      if (!Array.isArray(currentImages)) {
        currentImages = [form.value.images]
      }
    } catch {
      currentImages = form.value.images.split(',').map(u => u.trim()).filter(u => u)
    }
  }
  for (const imageUrl of urls) {
    if (!currentImages.includes(imageUrl)) {
      currentImages.push(imageUrl)
    }
  }
  form.value.images = JSON.stringify(currentImages)
  for (const imageUrl of urls) {
    if (!imageList.value.find(item => item.url === imageUrl)) {
      imageList.value.push({
        url: imageUrl,
        name: imageUrl.split('/').pop() || 'image'
      })
    }
  }
  imageUrlInput.value = ''
  ElMessage.success(`已添加 ${urls.length} 张图片`)
}

const publish = async () => {
  if (!form.value.title) {
    ElMessage.warning('请输入书名')
    return
  }
  if (!form.value.price) {
    ElMessage.warning('请输入售价')
    return
  }
  
  loading.value = true
  try {
    let hasImage = false
    if (form.value.images && form.value.images !== '' && form.value.images !== '[]') {
      try {
        const imgArr = JSON.parse(form.value.images)
        hasImage = Array.isArray(imgArr) && imgArr.length > 0
      } catch {
        hasImage = form.value.images.trim().length > 0
      }
    }
    
    let images = form.value.images
    if (!hasImage) {
      const defaultCover = generateDefaultCover()
      images = JSON.stringify([defaultCover])
      const color = getCategoryColor()
      ElMessage.info(`已自动生成${color.name}类专属封面：${form.value.title}`)
    }
    
    const submitData = {
      title: form.value.title,
      isbn: form.value.isbn,
      author: form.value.author,
      categoryId: form.value.categoryId,
      originalPrice: form.value.originalPrice,
      price: form.value.price,
      conditionLevel: form.value.conditionLevel,
      images: images,
      description: form.value.description
    }
    
    await publishBook(submitData)
    ElMessage.success('发布成功！等待审核')
    router.push('/book/list')
  } catch (err) {
    console.error('发布失败', err)
    ElMessage.error('发布失败，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.publish-container {
  width: 100%;
  max-width: 800px;
  margin: 24px auto;
  padding: 0 16px;
  box-sizing: border-box;
}
.publish-card {
  width: 100%;
  border: 2px solid #8c2323 !important;
  border-radius: 10px !important;
  background-color: #fffdf8 !important;
  box-shadow: 0 4px 12px rgba(140, 35, 35, 0.1);
}
.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #8c2323;
  margin: 0;
  letter-spacing: 1px;
  font-family: 'MaShanZheng', cursive;
}
.publish-form {
  padding: 24px 16px 10px;
}
:deep(.el-form-item) {
  margin-bottom: 20px;
}
:deep(.el-input__inner),
:deep(.el-textarea__inner) {
  height: 48px;
  border-radius: 8px;
  border: 1px solid #e2d5c0;
  background: #fdf9f2 !important;
  font-size: 15px;
}
:deep(.el-textarea__inner) {
  height: auto;
}
:deep(.el-input.is-focus .el-input__inner),
:deep(.el-textarea.is-focus .el-textarea__inner) {
  border-color: #8c2323;
  box-shadow: 0 0 0 3px rgba(140, 35, 35, 0.1);
}
:deep(.el-select__wrapper) {
  height: 48px;
  border-radius: 8px;
  border: 1px solid #e2d5c0;
  background-color: #fdf9f2 !important;
}
:deep(.el-select__content) {
  background-color: #fdf9f2 !important;
}
:deep(.el-select__input) {
  background-color: #fdf9f2 !important;
}
:deep(.el-select__placeholder) {
  background-color: #fdf9f2 !important;
}
:deep(.el-select__wrapper:hover) {
  background-color: #fdf9f2 !important;
}
:deep(.el-select.is-focus .el-select__wrapper) {
  border-color: #8c2323;
  box-shadow: 0 0 0 3px rgba(140, 35, 35, 0.1);
}
:deep(.el-upload--picture-card) {
  background: #fdf9f2 !important;
  border: 1px solid #e2d5c0;
}
:deep(.el-upload--picture-card:hover) {
  border-color: #8c2323;
}
.upload-tip {
  font-size: 12px;
  color: #703922;
  margin-top: 8px;
  line-height: 1.5;
}
.default-tip {
  color: #8c2323;
  font-weight: 500;
}
.img-preview-dialog :deep(.el-dialog__body) {
  padding: 10px;
}
.url-add-btn {
  height: 48px;
  background-color: #8c2323;
  border-color: #8c2323;
}
.url-add-btn:hover {
  background-color: #a02c2c !important;
  border-color: #a02c2c !important;
}
.btn-group {
  margin-top: 10px;
  display: flex;
  gap: 20px;
}
.submit-btn,
.cancel-btn {
  flex: 1;
  height: 48px;
  border-radius: 8px;
  font-size: 16px;
}
.submit-btn {
  background-color: #8c2323 !important;
  border-color: #8c2323 !important;
}
.submit-btn:hover {
  background-color: #a02c2c !important;
  border-color: #a02c2c !important;
}
.cancel-btn {
  border: 1px solid #8c2323;
  color: #8c2323;
}
.cancel-btn:hover {
  background-color: #f8f5eb;
  border-color: #8c2323;
}
@media (max-width: 768px) {
  .publish-container {
    padding: 0 8px;
    margin: 16px auto;
  }
  .btn-group {
    flex-direction: column;
    gap: 12px;
  }
}
@media (max-width: 576px) {
  :deep(.el-form-item) {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
  }
  :deep(.el-form-item__label) {
    margin-bottom: 6px;
  }
}
</style>