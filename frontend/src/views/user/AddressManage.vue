<template>
  <Layout>
    <div class="address-manage">
      <el-card class="address-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">收货地址管理</span>
            <el-button type="primary" class="add-btn" @click="showAddDialog">
              新增地址
            </el-button>
          </div>
        </template>
        <div class="address-list" v-if="addressList.length > 0">
          <div
            v-for="address in addressList"
            :key="address.id"
            class="address-item"
          >
            <div class="address-content">
              <div class="address-info">
                <span class="name">{{ address.name }}</span>
                <span class="phone">{{ address.phone }}</span>
                <el-tag v-if="address.isDefault === 1" type="success" size="small" class="default-tag">
                  默认地址
                </el-tag>
              </div>
              <div class="address-detail">{{ address.detail }}</div>
            </div>
            <div class="address-actions">
              <el-button
                v-if="address.isDefault !== 1"
                type="primary"
                text
                class="action-btn"
                @click="handleSetDefault(address.id)"
              >
                设为默认
              </el-button>
              <el-button type="primary" text class="action-btn" @click="handleEdit(address)">
                编辑
              </el-button>
              <el-button type="danger" text class="action-btn" @click="handleDelete(address.id)">
                删除
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无收货地址，点击右上角新增地址" />
      </el-card>

      <el-dialog
        v-model="showDialog"
        :title="isEdit ? '编辑地址' : '新增地址'"
        width="500px"
        class="address-dialog"
      >
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
          class="address-form"
        >
          <el-form-item label="收货人" prop="name">
            <el-input v-model="form.name" placeholder="请输入收货人姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="详细地址" prop="detail">
            <el-input
              v-model="form.detail"
              type="textarea"
              :rows="3"
              placeholder="请输入详细地址（省市区+街道门牌号）"
            />
          </el-form-item>
          <el-form-item label="默认地址">
            <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" active-color="#8c2323" />
            <span class="switch-tip">设为默认地址后，下单时自动选中</span>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showDialog = false" class="cancel-btn">取消</el-button>
          <el-button type="primary" :loading="loading" class="save-btn" @click="handleSubmit">
            保存
          </el-button>
        </template>
      </el-dialog>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { getAddressList, addAddress, updateAddress, deleteAddress, setDefaultAddress } from '@/api/user'

const addressList = ref([])
const showDialog = ref(false)
const isEdit = ref(false)
const loading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  phone: '',
  detail: '',
  isDefault: 0
})

const rules = {
  name: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  detail: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ]
}

const fetchAddressList = async () => {
  try {
    const res = await getAddressList()
    addressList.value = res.data
  } catch (error) {
    console.error('获取地址列表失败:', error)
    ElMessage.error('地址列表加载失败')
  }
}

const showAddDialog = () => {
  isEdit.value = false
  form.id = null
  form.name = ''
  form.phone = ''
  form.detail = ''
  form.isDefault = 0
  showDialog.value = true
}

const handleEdit = (address) => {
  isEdit.value = true
  form.id = address.id
  form.name = address.name
  form.phone = address.phone
  form.detail = address.detail
  form.isDefault = address.isDefault
  showDialog.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        if (isEdit.value) {
          await updateAddress(form)
          ElMessage.success('地址修改成功')
        } else {
          await addAddress(form)
          ElMessage.success('地址添加成功')
        }
        showDialog.value = false
        fetchAddressList()
      } catch (error) {
        console.error('保存失败:', error)
        ElMessage.error(error.response?.data?.message || '保存失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个收货地址吗？', '提示', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'confirm-btn-danger'
    })
    await deleteAddress(id)
    ElMessage.success('地址删除成功')
    fetchAddressList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

const handleSetDefault = async (id) => {
  try {
    await setDefaultAddress(id)
    ElMessage.success('已设为默认地址')
    fetchAddressList()
  } catch (error) {
    console.error('设置默认地址失败:', error)
    ElMessage.error('设置失败，请稍后重试')
  }
}

onMounted(() => {
  fetchAddressList()
})
</script>

<style scoped>
.address-manage {
  width: 100%;
  max-width: 900px;
  margin: 24px auto;
  padding: 0 16px;
  box-sizing: border-box;
}

/* 统一国风卡片样式 */
.address-card {
  width: 100%;
  border: 2px solid #8c2323 !important;
  border-radius: 10px !important;
  background-color: #fffdf8 !important;
  box-shadow: 0 4px 12px rgba(140, 35, 35, 0.1);
}

/* 卡片头部 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.card-title {
  font-size: 20px;
  font-weight: 600;
  color: #8c2323;
  font-family: 'MaShanZheng', cursive;
  letter-spacing: 1px;
}
.add-btn {
  height: 40px;
  padding: 0 20px;
  border-radius: 8px;
  background-color: #8c2323 !important;
  border-color: #8c2323 !important;
}
.add-btn:hover {
  background-color: #a02c2c !important;
  border-color: #a02c2c !important;
}

/* 地址列表项 */
.address-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 8px 0;
}
.address-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  border: 1px solid #e2d5c0;
  border-radius: 8px;
  background: #fff;
  transition: all 0.2s;
}
.address-item:hover {
  border-color: #8c2323;
  box-shadow: 0 2px 8px rgba(140, 35, 35, 0.1);
}

.address-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}
.address-info .name {
  font-weight: 600;
  color: #333;
  font-size: 16px;
}
.address-info .phone {
  color: #703922;
  font-size: 14px;
}
.default-tag {
  background-color: #2e7d32 !important;
  border-color: #2e7d32 !important;
}
.address-detail {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.address-actions {
  display: flex;
  gap: 12px;
}
.action-btn {
  font-size: 14px;
}

/* 弹窗样式统一 */
:deep(.address-dialog .el-dialog__header) {
  background-color: #fffdf8;
  border-bottom: 1px solid #e2d5c0;
  padding: 18px 24px;
}
:deep(.address-dialog .el-dialog__title) {
  color: #8c2323;
  font-family: 'MaShanZheng', cursive;
  font-size: 18px;
}
:deep(.address-dialog .el-dialog__body) {
  background-color: #fffdf8;
  padding: 24px;
}
:deep(.address-dialog .el-dialog__footer) {
  background-color: #fffdf8;
  border-top: 1px solid #e2d5c0;
  padding: 16px 24px;
}

/* 表单样式统一 */
.address-form {
  width: 100%;
}
:deep(.address-form .el-input__wrapper) {
  height: 44px;
  border-radius: 8px;
  border: 1px solid #e2d5c0;
  background-color: #fdf9f2 !important;
}
:deep(.address-form .el-input__wrapper.is-focus) {
  border-color: #8c2323;
  box-shadow: 0 0 0 3px rgba(140, 35, 35, 0.1);
}
:deep(.address-form .el-textarea__inner) {
  border-radius: 8px;
  border: 1px solid #e2d5c0;
  background-color: #fdf9f2 !important;
}
:deep(.address-form .el-textarea__inner:focus) {
  border-color: #8c2323;
  box-shadow: 0 0 0 3px rgba(140, 35, 35, 0.1);
}
.switch-tip {
  margin-left: 10px;
  font-size: 13px;
  color: #999;
}

.cancel-btn {
  height: 42px;
  padding: 0 24px;
  border-radius: 8px;
  border-color: #e2d5c0;
  color: #703922;
}
.cancel-btn:hover {
  border-color: #8c2323;
  color: #8c2323;
}
.save-btn {
  height: 42px;
  padding: 0 28px;
  border-radius: 8px;
  background-color: #8c2323 !important;
  border-color: #8c2323 !important;
}
.save-btn:hover {
  background-color: #a02c2c !important;
  border-color: #a02c2c !important;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .address-manage {
    padding: 0 8px;
    margin: 16px auto;
  }
  .address-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .address-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>