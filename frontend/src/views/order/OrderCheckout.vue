<template>
  <Layout>
    <div class="order-checkout">
      <el-card class="checkout-card">
        <template #header>
          <span class="card-title">确认订单</span>
        </template>
        <el-form :model="form" label-width="100px" class="checkout-form">
          <el-form-item label="收货地址">
            <el-select v-model="form.addressId" placeholder="请选择收货地址" class="addr-select">
              <el-option
                v-for="addr in addressList"
                :key="addr.id"
                :label="`${addr.name} ${addr.phone} ${addr.detail}`"
                :value="addr.id"
              />
            </el-select>
            <div class="address-tip">
              <el-link type="primary" @click="router.push('/user/address')">管理收货地址</el-link>
            </div>
          </el-form-item>

          <el-divider>教材清单</el-divider>

          <div class="book-list">
            <div v-for="item in selectedBooks" :key="item.id" class="book-item">
              <el-image
                :src="parseImages(item.book?.images)[0] || ''"
                fit="cover"
                class="book-image"
              >
                <template #error>
                  <div class="image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="book-info">
                <div class="book-title">{{ item.book?.title || '' }}</div>
                <div class="book-condition">成色: {{ item.book?.conditionLevel || '未知' }}</div>
                <el-tag
                  v-if="(item.book?.stock || 0) === 0"
                  type="danger"
                  size="small"
                  style="margin-top: 4px;"
                >
                  库存不足
                </el-tag>
                <el-tag
                  v-else-if="(item.book?.stock || 0) > 0 && (item.book?.stock || 0) <= 3"
                  type="warning"
                  size="small"
                  style="margin-top: 4px;"
                >
                  仅剩 {{ item.book.stock }} 本
                </el-tag>
              </div>
              <div class="book-price">¥{{ item.book?.price || 0 }}</div>
            </div>
          </div>

          <el-empty
            v-if="selectedBooks.length === 0"
            description="没有要结算的教材"
            class="empty-books"
          />

          <el-alert
            v-if="hasOutOfStock()"
            title="部分教材库存不足"
            type="warning"
            description="请移除库存不足的教材后再提交"
            :closable="false"
            class="stock-alert"
          />

          <el-divider />

          <div class="total">
            <span>合计：</span>
            <span class="total-price">¥{{ totalPrice() }}</span>
          </div>

          <el-form-item class="submit-item">
            <el-button
              type="success"
              size="large"
              class="direct-pay-btn"
              :loading="submitting"
              @click="handleDirectPay"
            >
              直接支付
            </el-button>
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="submitting"
              @click="handleSubmit"
            >
              提交订单
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 支付弹窗 -->
    <PaymentModal
      v-model="payModalVisible"
      :order-info="currentPayOrder"
      @success="handlePaySuccess"
      @cancel="payModalVisible = false"
    />
  </Layout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import Layout from '@/components/Layout.vue'
import PaymentModal from './PaymentModal.vue'

import { getCartList } from '@/api/cart'
import { getAddressList } from '@/api/user'
import { getBookById } from '@/api/book'
import { createOrder, createBatchOrder, payOrder } from '@/api/order'

const router = useRouter()
const route = useRoute()

const form = ref({ addressId: null })
const addressList = ref([])
const selectedBooks = ref([])
const submitting = ref(false)

const payModalVisible = ref(false)
const currentPayOrder = ref({})
// 单个订单ID（单品使用）
const realOrderId = ref(null)
// 批量所有订单ID数组（批量下单专用）
const batchOrderList = ref([])

const parseImages = (images) => {
  if (!images) return []
  try {
    const arr = JSON.parse(images)
    return Array.isArray(arr) ? arr : []
  } catch {
    return images.split(',').map(u => u.trim()).filter(u => u)
  }
}

const totalPrice = () => {
  const total = selectedBooks.value.reduce((sum, item) => {
    return sum + Number(item.book?.price || 0)
  }, 0)
  return total.toFixed(2)
}

const hasOutOfStock = () => {
  return selectedBooks.value.some(item => (item.book?.stock || 0) === 0)
}

const loadData = async () => {
  const bookId = route.query.bookId
  if (bookId) {
    try {
      const res = await getBookById(bookId)
      if (res && res.data) {
        selectedBooks.value = [{
          id: 0,
          bookId: res.data.id,
          book: res.data
        }]
      }
    } catch (err) {
      ElMessage.error('商品信息加载失败')
      console.error(err)
    }
  } else {
    try {
      const cartRes = await getCartList()
      const allCart = (cartRes && cartRes.data) ? cartRes.data : []
      let selectIds = []
      const idsStr = localStorage.getItem('checkoutCartIds')
      if (idsStr) {
        selectIds = JSON.parse(idsStr).map(Number)
      }
      selectedBooks.value = allCart.filter(item => selectIds.includes(Number(item.id)))
    } catch (err) {
      ElMessage.error('数据加载失败')
      console.error(err)
    }
  }

  try {
    const addrRes = await getAddressList()
    const addrList = (addrRes && addrRes.data) ? addrRes.data : []
    addressList.value = addrList
    const defaultAddr = addrList.find(addr => addr.isDefault === 1)
    if (defaultAddr) {
      form.value.addressId = defaultAddr.id
    } else if (addrList.length > 0) {
      form.value.addressId = addrList[0].id
    }
  } catch (err) {
    console.error('地址加载失败', err)
  }
}

const handleDirectPay = async () => {
  if (!form.value.addressId) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (selectedBooks.value.length === 0) {
    ElMessage.warning('请选择要购买的教材')
    return
  }
  if (hasOutOfStock()) {
    ElMessage.error('存在库存不足的教材，请移除后再提交')
    return
  }

  submitting.value = true
  // 清空上一次批量订单数据
  batchOrderList.value = []
  realOrderId.value = null

  try {
    const bookId = route.query.bookId
    if (bookId) {
      // 单品下单
      const res = await createOrder({
        bookId: parseInt(bookId),
        addressId: form.value.addressId
      })
      if (res.code !== 200) {
        ElMessage.error('订单创建失败')
        return
      }
      realOrderId.value = res.data?.id
      currentPayOrder.value = {
        orderNo: res.data?.orderNo || 'ORD' + Date.now(),
        goodsName: selectedBooks.value[0].book.title,
        amount: totalPrice()
      }
      payModalVisible.value = true
    } else {
      // 批量下单
      const cartIds = selectedBooks.value.map(item => Number(item.id))
      const res = await createBatchOrder({
        addressId: form.value.addressId,
        cartIds: cartIds
      })
      if (res.code !== 200) {
        ElMessage.error('订单创建失败')
        return
      }
      // 存储所有批量订单ID
      const orderArr = res.data?.successOrders || []
      batchOrderList.value = orderArr.map(item => item.id)
      // 取第一条用于弹窗展示
      const firstOrder = orderArr[0]
      realOrderId.value = firstOrder.id
      currentPayOrder.value = {
        orderNo: firstOrder.orderNo,
        goodsList: selectedBooks.value.map(item => ({
          name: item.book.title,
          quantity: 1
        })),
        amount: totalPrice()
      }
      localStorage.removeItem('checkoutCartIds')
      payModalVisible.value = true
    }
  } catch (err) {
    console.error('下单失败', err)
    ElMessage.error(err.response?.data?.message || '下单失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

// 支付成功回调：区分单品 / 批量，批量循环支付所有订单
const handlePaySuccess = async () => {
  payModalVisible.value = false
  // 批量订单优先处理
  if (batchOrderList.value.length > 0) {
    let successCount = 0
    let failCount = 0
    for (const orderId of batchOrderList.value) {
      try {
        await payOrder(orderId)
        successCount++
      } catch (err) {
        console.error(`订单${orderId}支付失败`, err)
        failCount++
      }
    }
    if (failCount === 0) {
      ElMessage.success(`共${successCount}笔订单，全部支付完成！`)
    } else {
      ElMessage.warning(`${successCount}笔支付成功，${failCount}笔失败，请前往订单列表手动处理`)
    }
  } else {
    // 单个订单
    if (!realOrderId.value || typeof realOrderId.value !== 'number') {
      ElMessage.warning('无有效订单编号，无法支付')
      return
    }
    try {
      await payOrder(realOrderId.value)
      ElMessage.success('订单支付完成！')
    } catch (err) {
      console.error('支付失败', err)
      ElMessage.error('支付操作失败，请稍后重试')
    }
  }
  // 清空缓存，跳转订单列表
  batchOrderList.value = []
  realOrderId.value = null
  router.push('/order/list')
}

// 仅提交订单（待后续付款）
const handleSubmit = async () => {
  if (!form.value.addressId) {
    ElMessage.warning('请选择收货地址')
    return
  }
  if (selectedBooks.value.length === 0) {
    ElMessage.warning('请选择要购买的教材')
    return
  }
  if (hasOutOfStock()) {
    ElMessage.error('存在库存不足的教材，请移除后再提交')
    return
  }

  submitting.value = true
  try {
    const bookId = route.query.bookId
    if (bookId) {
      await createOrder({
        bookId: parseInt(bookId),
        addressId: form.value.addressId
      })
    } else {
      const cartIds = selectedBooks.value.map(item => Number(item.id))
      await createBatchOrder({
        addressId: form.value.addressId,
        cartIds: cartIds
      })
      localStorage.removeItem('checkoutCartIds')
    }
    ElMessage.success('下单成功，请前往订单列表支付')
    router.push('/order/list')
  } catch (err) {
    console.error('提交订单失败', err)
    ElMessage.error(err.response?.data?.message || '下单失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.order-checkout {
  width: 100%;
  max-width: 1000px;
  margin: 24px auto;
  padding: 0 16px;
  box-sizing: border-box;
}
.checkout-card {
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
}
.checkout-form {
  padding: 20px 16px 10px;
}
.addr-select {
  width: 100%;
}
:deep(.addr-select .el-select__wrapper) {
  height: 44px;
  border-radius: 8px;
  border: 1px solid #e2d5c0;
  background-color: #fdf9f2 !important;
}
:deep(.addr-select.is-focus .el-select__wrapper) {
  border-color: #8c2323;
  box-shadow: 0 0 0 3px rgba(140, 35, 35, 0.1);
}
.address-tip {
  margin-top: 8px;
}
:deep(.address-tip .el-link) {
  color: #8c2323;
}
:deep(.el-divider) {
  border-color: #e2d5c0;
  margin: 16px 0;
}
:deep(.el-divider__text) {
  color: #8c2323;
  font-weight: 500;
}
.book-list {
  margin: 16px 0;
}
.book-item {
  display: flex;
  align-items: center;
  padding: 14px 8px;
  border-bottom: 1px solid #e2d5c0;
}
.book-image {
  width: 50px;
  height: 65px;
  border-radius: 4px;
  margin-right: 16px;
  border: 1px solid #e2d5c0;
}
.image-placeholder {
  width: 50px;
  height: 65px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f0e3;
  border-radius: 4px;
  color: #b88646;
}
.book-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.book-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.book-condition {
  font-size: 12px;
  color: #703922;
}
.book-price {
  font-size: 16px;
  font-weight: 600;
  color: #c83030;
}
.empty-books {
  padding: 30px 0;
}
.stock-alert {
  margin-top: 16px;
}
.total {
  text-align: right;
  padding: 16px 0;
  font-size: 18px;
}
.total-price {
  font-size: 24px;
  font-weight: 700;
  color: #c83030;
  margin-left: 8px;
}
.submit-item {
  text-align: right;
  margin-top: 20px;
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}
.submit-btn {
  height: 48px;
  padding: 0 40px;
  border-radius: 8px;
  background-color: #8c2323 !important;
  border-color: #8c2323 !important;
}
.submit-btn:hover {
  background-color: #a02c2c !important;
  border-color: #a02c2c !important;
}
.direct-pay-btn {
  height: 48px;
  padding: 0 40px;
  border-radius: 8px;
  background-color: #2e7d32 !important;
  border-color: #2e7d32 !important;
}
.direct-pay-btn:hover {
  background-color: #388e3c !important;
  border-color: #388e3c !important;
}
@media (max-width: 768px) {
  .order-checkout {
    margin: 16px auto;
    padding: 0 8px;
  }
  .book-item {
    padding: 12px 0;
  }
  .submit-item {
    flex-direction: column;
  }
  .submit-item .el-button {
    width: 100%;
  }
}
</style>