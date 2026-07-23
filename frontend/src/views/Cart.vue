<template>
  <Layout>
    <div class="cart-container">
      <el-card class="cart-card">
        <template #header>
          <div class="cart-header">
            <h2 class="cart-title">我的暂存架</h2>
            <el-button 
              type="primary" 
              :disabled="selectedItems.length === 0" 
              @click="handleBatchCheckout"
              class="checkout-btn"
            >
              批量结算
            </el-button>
          </div>
        </template>
        <div class="table-wrapper">
          <el-table 
            :data="cartListWithBook" 
            @selection-change="handleSelectionChange"
            border
            size="medium"
            class="cart-table"
            :empty-text="''"
          >
            <el-table-column type="selection" width="55" align="center" />
            
            <el-table-column label="教材信息" min-width="200" align="center">
              <template #default="{ row }">
                <div class="book-info">
                  <el-image
                    :src="parseImages(row.images)[0] || ''"
                    fit="cover"
                    class="book-image"
                    lazy
                  >
                    <template #error>
                      <div class="image-placeholder">
                        <el-icon size="20"><Picture /></el-icon>
                      </div>
                    </template>
                  </el-image>
                  <div class="book-detail">
                    <div class="book-title" :title="row.title">{{ row.title }}</div>
                    <div class="book-condition">成色: {{ row.conditionLevel || '未知' }}</div>
                  </div>
                </div>
              </template>
            </el-table-column>
            
            <el-table-column label="数量" width="80" align="center">
              <template #default="{ row }">
                <span class="quantity">{{ row.quantity || 1 }}</span>
              </template>
            </el-table-column>
            
            <el-table-column label="单价" width="90" align="center">
              <template #default="{ row }">
                <span class="price">¥{{ row.price }}</span>
              </template>
            </el-table-column>
            
            <el-table-column label="库存" width="110" align="center">
              <template #default="{ row }">
                <el-tag 
                  :type="(row.stock || 0) > 0 ? 'success' : 'danger'" 
                  size="small"
                  class="stock-tag"
                >
                  {{ (row.stock || 0) > 0 ? `有货 (${row.stock})` : '缺货' }}
                </el-tag>
              </template>
            </el-table-column>
            
            <el-table-column label="小计" width="100" align="center">
              <template #default="{ row }">
                <span class="price">¥{{ (row.price * (row.quantity || 1)).toFixed(2) }}</span>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="200" align="center">
              <template #default="{ row }">
                <div class="operate-group">
                  <el-tag 
                    v-if="(row.stock || 0) === 0" 
                    type="warning" 
                    size="small" 
                    class="out-of-stock-tag"
                  >
                    无库存
                  </el-tag>
                  <el-button 
                    type="primary" 
                    link 
                    size="small"
                    @click="handleSingleCheckout(row)"
                    :disabled="(row.stock || 0) === 0"
                    class="single-checkout-btn"
                  >
                    结算
                  </el-button>
                  <el-button 
                    type="danger" 
                    link 
                    size="small"
                    @click="handleDelete(row.cartId)"
                    class="delete-btn"
                  >
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <el-empty 
            v-if="cartListWithBook.length === 0" 
            description="暂存架是空的，快去添加教材吧"
            class="empty-cart"
          >
            <el-button type="primary" class="empty-btn" @click="router.push('/book/list')">去添加教材</el-button>
          </el-empty>
        </div>
      </el-card>
    </div>
  </Layout>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import { getCartList, removeFromCart } from '@/api/cart'
import { getBookById } from '@/api/book'
const router = useRouter()
const cartList = ref([])
const selectedItems = ref([])
const bookMap = ref({})

// ✅ 修复：合并对象时区分cartId和bookId，避免id覆盖
const cartListWithBook = computed(() => {
  return cartList.value.map(cart => {
    const book = bookMap.value[cart.bookId] || {}
    return {
      ...cart,
      ...book,
      cartId: cart.id, // 暂存架条目id
      bookId: book.id  // 教材id
    }
  })
})

const parseImages = (images) => {
  try {
    if (!images) return []
    const trimmed = images.trim()
    if (!trimmed) return []
    
    const parsed = JSON.parse(trimmed)
    if (Array.isArray(parsed)) return parsed
    if (typeof parsed === 'string') return [parsed]
    
    return []
  } catch {
    if (typeof images === 'string') {
      const urls = images.split(',').map(url => url.trim()).filter(url => url)
      return urls
    }
    return []
  }
}
const loadCart = async () => {
  try {
    const res = await getCartList()
    cartList.value = res.data || []
    
    for (const cart of cartList.value) {
      if (!bookMap.value[cart.bookId]) {
        const bookRes = await getBookById(cart.bookId)
        bookMap.value[cart.bookId] = bookRes.data || {}
      }
    }
  } catch (error) {
    console.error('加载暂存架失败:', error)
    ElMessage.error('加载暂存架失败，请稍后重试')
  }
}
const handleSelectionChange = (selection) => {
  selectedItems.value = selection
}
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要从暂存架中移除该教材吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeFromCart(id)
    ElMessage.success('移除成功')
    loadCart()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('移除失败:', error)
      ElMessage.error('移除失败，请稍后重试')
    }
  }
}

// ✅ 修复：单独结算使用cartId
const handleSingleCheckout = (row) => {
  localStorage.setItem('checkoutCartIds', JSON.stringify([row.cartId]))
  router.push('/order/checkout')
}

// ✅ 修复：批量结算使用cartId
const handleBatchCheckout = () => {
  const cartIds = selectedItems.value.map(item => item.cartId)
  localStorage.setItem('checkoutCartIds', JSON.stringify(cartIds))
  router.push('/order/checkout')
}

onMounted(() => {
  loadCart()
})
</script>
<style scoped>
.cart-container {
  width: 100%;
  max-width: 1400px;
  margin: 24px auto;
  padding: 0 16px;
  box-sizing: border-box;
}
.cart-card {
  width: 100%;
  border: 2px solid #8c2323 !important;
  border-radius: 10px !important;
  background-color: #fffdf8 !important;
  box-shadow: 0 4px 12px rgba(140, 35, 35, 0.1);
  overflow: hidden;
}
.cart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}
.cart-title {
  font-size: 20px;
  font-weight: 600;
  color: #8c2323;
  margin: 0;
  letter-spacing: 1px;
}
.checkout-btn {
  height: 40px;
  border-radius: 8px;
  background-color: #8c2323 !important;
  border-color: #8c2323 !important;
}
.checkout-btn:hover {
  background-color: #a02c2c !important;
  border-color: #a02c2c !important;
}
.table-wrapper {
  margin-top: 16px;
  overflow-x: auto;
}
.cart-table {
  width: 100% !important;
  --el-table-header-bg-color: #f8f5eb;
  --el-table-header-text-color: #8c2323;
  --el-table-row-hover-bg-color: #f3e9d6;
  --el-table-border-color: #e2d5c0;
}
.book-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 8px;
}
.book-image {
  width: 60px;
  height: 80px;
  border-radius: 4px;
  border: 1px solid #e2d5c0;
  background: #f5f0e3;
}
.image-placeholder {
  width: 60px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f0e3;
  border: 1px solid #e2d5c0;
  border-radius: 4px;
  color: #b88646;
}
.book-detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  text-align: left;
}
.book-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
}
.book-condition {
  font-size: 12px;
  color: #703922;
}
.quantity {
  color: #333;
  font-size: 14px;
}
.price {
  color: #c83030;
  font-weight: 600;
  font-size: 14px;
}
.stock-tag {
  border-radius: 4px;
}
.operate-group {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}
.out-of-stock-tag {
  margin: 0;
}
.single-checkout-btn {
  font-size: 13px;
  color: #8c2323;
}
.single-checkout-btn:hover {
  color: #a02c2c;
}
.single-checkout-btn:disabled {
  color: #ccc;
  cursor: not-allowed;
}
.delete-btn {
  font-size: 13px;
  color: #f56c6c;
}
.delete-btn:hover {
  color: #e53935;
}
.empty-cart {
  padding: 40px 0;
  text-align: center;
}
:deep(.empty-cart .el-empty__description) {
  color: #703922;
  font-size: 14px;
}
.empty-btn {
  height: 38px;
  border-radius: 8px;
  background-color: #8c2323 !important;
  border-color: #8c2323 !important;
  margin-top: 12px;
}
.empty-btn:hover {
  background-color: #a02c2c !important;
  border-color: #a02c2c !important;
}
@media (max-width: 768px) {
  .cart-container {
    margin: 16px auto;
    padding: 0 8px;
  }
  .cart-title {
    font-size: 18px;
  }
  .book-image,
  .image-placeholder {
    width: 50px;
    height: 70px;
  }
  .book-title {
    font-size: 13px;
    -webkit-line-clamp: 1;
    line-clamp: 1;
  }
}
@media (max-width: 480px) {
  .cart-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .checkout-btn {
    width: 100%;
  }
  .book-info {
    gap: 8px;
  }
}
</style>