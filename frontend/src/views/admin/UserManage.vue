<template>
  <Layout>
    <div class="user-manage">
      <el-card class="user-card">
        <template #header>
          <div class="card-header">
            <span class="card-title">用户管理</span>
          </div>
        </template>
        <div class="table-scroll">
          <el-table 
            :data="userList" 
            v-loading="loading" 
            stripe 
            class="user-table"
          >
            <el-table-column prop="id" label="ID" width="80" align="center" />
            <el-table-column prop="username" label="用户名" width="120" align="center" />
            <el-table-column prop="nickname" label="昵称" min-width="140" align="center" />
            <el-table-column prop="phone" label="手机号" width="130" align="center" />
            <el-table-column prop="role" label="角色" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.role === 'admin' ? 'danger' : 'primary'" size="small">
                  {{ row.role === 'admin' ? '管理员' : '普通用户' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="balance" label="余额" width="100" align="center">
              <template #default="{ row }">
                <span class="balance-text">¥{{ row.balance || '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '正常' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="注册时间" width="180" align="center" />
            <el-table-column label="操作" fixed="right" width="150" align="center">
              <template #default="{ row }">
                <el-button
                  v-if="row.role !== 'admin'"
                  :type="row.status === 1 ? 'danger' : 'success'"
                  size="small"
                  text
                  @click="handleToggleStatus(row)"
                >
                  {{ row.status === 1 ? '禁用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="pagination">
          <el-pagination
            v-model:current-page="pagination.page"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="fetchUserList"
            @current-change="fetchUserList"
          />
        </div>
      </el-card>
    </div>
  </Layout>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Layout from '@/components/Layout.vue'
import { getUserList, updateUserStatus } from '@/api/admin'

const loading = ref(false)
const userList = ref([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const fetchUserList = async () => {
  loading.value = true
  try {
    const res = await getUserList({
      page: pagination.page,
      size: pagination.size,
      sort: 'id,asc' // 新增：按ID升序排序
    })
    userList.value = res.data.records
    pagination.total = res.data.total
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('数据加载失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '禁用' : '启用'
  
  try {
    await ElMessageBox.confirm(`确定要${action}用户 "${row.username}" 吗？`, '操作确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    fetchUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败，请稍后重试')
    }
  }
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-manage {
  width: 100%;
  max-width: 1600px;
  margin: 24px auto;
  padding: 0 16px;
  box-sizing: border-box;
}

.user-card {
  width: 100%;
  border: 2px solid #8c2323 !important;
  border-radius: 10px !important;
  background-color: #fffdf8 !important;
  box-shadow: 0 4px 12px rgba(140, 35, 35, 0.1);
  --el-card-padding: 20px 0;
}

.card-header {
  padding: 0 20px;
  display: flex;
  align-items: center;
}
.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #8c2323;
  letter-spacing: 1px;
}

.table-scroll {
  width: 100%;
  overflow-x: auto;
}

.user-table {
  width: 100% !important;
  table-layout: auto !important;
  --el-table-header-bg-color: #f8f5eb;
  --el-table-header-text-color: #8c2323;
  --el-table-row-hover-bg-color: #f3e9d6;
}

.balance-text {
  color: #c83030;
  font-weight: 500;
  font-size: 14px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  padding: 0 20px 20px;
}

@media (max-width: 768px) {
  .user-manage {
    margin: 16px auto;
    padding: 0 8px;
  }
  .pagination {
    justify-content: center;
  }
}
</style>