<template>
  <el-dialog
    v-model="dialogVisible"
    title="模拟支付"
    width="500px"
    :close-on-click-modal="false"
    class="payment-modal"
  >
    <div class="payment-content">
      <!-- 订单信息 -->
      <div class="order-info">
        <div class="order-row">
          <span class="label">订单号：</span>
          <span class="value">{{ orderInfo.orderNo }}</span>
        </div>
        <div class="order-row goods-row">
          <span class="label">商品：</span>
          <div class="value goods-list">
            <!-- 支持单个/多个商品显示 -->
            <div v-for="(item, index) in goodsList" :key="index" class="goods-item">
              {{ item.name }} ×{{ item.quantity }}
            </div>
          </div>
        </div>
        <div class="order-amount">
          <span class="amount-label">支付金额：</span>
          <span class="amount">¥{{ orderInfo.amount }}</span>
        </div>
      </div>
      <!-- 支付倒计时 -->
      <div class="countdown">
        <el-icon><Timer /></el-icon>
        <span>请在 <span class="time">{{ formatTime }}</span> 内完成支付</span>
      </div>
      <!-- 支付方式 -->
      <div class="pay-methods">
        <div class="method-title">选择支付方式</div>
        <div 
          v-for="method in payMethods" 
          :key="method.value"
          class="method-item"
          :class="{ active: selectedPayMethod === method.value }"
          @click="selectedPayMethod = method.value"
        >
          <span class="method-name">{{ method.name }}</span>
          <div class="custom-radio">
            <div class="radio-dot" v-if="selectedPayMethod === method.value"></div>
          </div>
        </div>
      </div>
      <!-- 操作按钮 -->
      <div class="pay-actions">
        <el-button @click="handleCancel" class="cancel-btn">取消支付</el-button>
        <el-button 
          type="primary" 
          :loading="paying"
          @click="handlePay"
          class="pay-btn"
        >
          {{ paying ? '支付中...' : '确认支付' }}
        </el-button>
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Timer } from '@element-plus/icons-vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  orderInfo: {
    type: Object,
    default: () => ({
      orderNo: '',
      goodsName: '',
      goodsList: [], // 新增：批量结算多商品列表
      amount: '0.00'
    })
  }
})

const emit = defineEmits(['update:visible', 'success', 'cancel'])

// 变量定义
const dialogVisible = ref(false)
const selectedPayMethod = ref('wechat')
const paying = ref(false)
const countdown = ref(15 * 60)
let timer = null

const payMethods = [
  { name: '微信支付', value: 'wechat' },
  { name: '支付宝', value: 'alipay' }
]

// 兼容单个商品和批量多商品两种场景
const goodsList = computed(() => {
  // 如果传了goodsList（批量结算）就用列表
  if (props.orderInfo.goodsList && props.orderInfo.goodsList.length > 0) {
    return props.orderInfo.goodsList
  }
  // 否则兼容原有单个商品格式（向后兼容）
  return [{
    name: props.orderInfo.goodsName || '',
    quantity: 1
  }]
})

// 时间格式化
const formatTime = computed(() => {
  const m = Math.floor(countdown.value / 60).toString().padStart(2, '0')
  const s = (countdown.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})

// 倒计时函数
function startCountdown() {
  countdown.value = 15 * 60
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
      ElMessage.error('支付超时，订单已取消')
      handleCancel()
    }
  }, 1000)
}

function stopCountdown() {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

// ✅ 统一关闭弹窗函数（确保父子状态同步）
function closeModal() {
  stopCountdown()
  dialogVisible.value = false
  emit('update:visible', false)
}

// 弹窗状态双向同步
watch(() => props.visible, (newVal) => {
  dialogVisible.value = newVal
  if (newVal) {
    startCountdown()
  } else {
    stopCountdown()
  }
}, { immediate: true })

watch(dialogVisible, (newVal) => {
  emit('update:visible', newVal)
})

onUnmounted(() => {
  stopCountdown()
})

// ✅ 修复：支付成功后正确关闭弹窗
const handlePay = () => {
  paying.value = true
  setTimeout(() => {
    paying.value = false
    ElMessage.success('支付成功！')
    // 1. 先触发成功回调
    emit('success')
    // 2. 统一关闭弹窗，确保父子组件状态100%同步
    closeModal()
  }, 2000)
}

const handleCancel = () => {
  emit('cancel')
  closeModal()
}
</script>

<style scoped>
.payment-modal :deep(.el-dialog__header) {
  background-color: #fffdf8;
  border-bottom: 1px solid #e2d5c0;
  padding: 16px 20px;
}
.payment-modal :deep(.el-dialog__title) {
  color: #8c2323;
  font-weight: 600;
  font-size: 18px;
}
.payment-modal :deep(.el-dialog__body) {
  padding: 20px;
  background-color: #fffdf8;
}
.payment-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
/* 订单信息 */
.order-info {
  padding: 16px;
  background: #f8f5eb;
  border-radius: 8px;
  border: 1px solid #e2d5c0;
}
.order-row {
  display: flex;
  margin-bottom: 8px;
  font-size: 14px;
}
.order-row .label {
  color: #703922;
  width: 70px;
  flex-shrink: 0;
}
.order-row .value {
  color: #333;
  flex: 1;
}
/* 多商品列表样式 */
.goods-row {
  align-items: flex-start;
}
.goods-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.goods-item {
  line-height: 1.5;
}
.order-amount {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e2d5c0;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.amount-label {
  color: #703922;
  font-size: 14px;
}
.amount {
  font-size: 24px;
  font-weight: 700;
  color: #c83030;
}
/* 倒计时 */
.countdown {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px;
  background: #fff7e6;
  border-radius: 6px;
  color: #d48806;
  font-size: 14px;
}
.countdown .time {
  font-weight: 600;
  color: #c83030;
}
/* 支付方式 */
.pay-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.method-title {
  font-size: 14px;
  color: #703922;
  font-weight: 500;
}
.method-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border: 1px solid #e2d5c0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;
}
.method-item:hover {
  border-color: #8c2323;
  background: #fdf9f2;
}
.method-item.active {
  border-color: #8c2323;
  background: #fdf9f2;
  box-shadow: 0 0 0 2px rgba(140, 35, 35, 0.1);
}
.method-name {
  font-size: 15px;
  color: #333;
}
.custom-radio {
  width: 18px;
  height: 18px;
  border: 2px solid #dcdfe6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.method-item.active .custom-radio {
  border-color: #8c2323;
}
.radio-dot {
  width: 10px;
  height: 10px;
  background-color: #8c2323;
  border-radius: 50%;
}
/* 操作按钮 */
.pay-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 8px;
}
.cancel-btn {
  height: 44px;
  padding: 0 24px;
  border-radius: 8px;
  border-color: #e2d5c0;
  color: #703922;
}
.cancel-btn:hover {
  border-color: #8c2323;
  color: #8c2323;
}
.pay-btn {
  height: 44px;
  padding: 0 32px;
  border-radius: 8px;
  background-color: #8c2323 !important;
  border-color: #8c2323 !important;
}
.pay-btn:hover {
  background-color: #a02c2c !important;
  border-color: #a02c2c !important;
}
@media (max-width: 520px) {
  .payment-modal :deep(.el-dialog) {
    width: 94% !important;
    margin: 5vh auto;
  }
  .pay-actions {
    flex-direction: column;
  }
  .pay-actions .el-button {
    width: 100%;
  }
}
</style>