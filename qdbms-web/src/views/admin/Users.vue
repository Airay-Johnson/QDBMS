<template>
  <div class="page-container">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索用户名或邮箱" clearable style="width:280px"
        @keydown.enter="fetchData" @clear="fetchData" />
      <el-button type="primary" @click="fetchData">搜索</el-button>
    </div>

    <el-card>
      <el-table :data="users" stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="140" />
        <el-table-column prop="email" label="邮箱" min-width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isActive ? 'success' : 'danger'" size="small">
              {{ row.isActive ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="openRoleDialog(row)">
              角色
            </el-button>
            <el-button size="small" text :type="row.isActive ? 'warning' : 'success'"
              @click="toggleActive(row)">
              {{ row.isActive ? '禁用' : '启用' }}
            </el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button size="small" text type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top:16px;text-align:right">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="fetchData"
        />
      </div>
    </el-card>

    <!-- 角色分配弹窗 -->
    <el-dialog v-model="roleDialog" title="分配角色" width="440px">
      <el-checkbox-group v-model="selectedRoles">
        <el-checkbox v-for="role in allRoles" :key="role.id" :label="role.id">
          {{ role.roleName }} — <span style="color:#999;font-size:12px">{{ role.description }}</span>
        </el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="roleDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAssignRoles">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listUsers, updateUser, deleteUser, assignRoles, getUserRoles } from '@/api/user'
import { listRoles } from '@/api/user'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const users = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const keyword = ref('')

const roleDialog = ref(false)
const currentUser = ref(null)
const allRoles = ref([])
const selectedRoles = ref([])

async function fetchData() {
  loading.value = true
  try {
    const data = await listUsers({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    users.value = data?.records || []
    total.value = data?.total || 0
  } finally {
    loading.value = false
  }
}

async function toggleActive(row) {
  await updateUser(row.id, { isActive: !row.isActive })
  ElMessage.success(row.isActive ? '已禁用' : '已启用')
  fetchData()
}

async function handleDelete(row) {
  await deleteUser(row.id)
  ElMessage.success('已删除')
  fetchData()
}

async function openRoleDialog(row) {
  currentUser.value = row
  allRoles.value = await listRoles() || []
  const roles = await getUserRoles(row.id) || []
  selectedRoles.value = allRoles.value
    .filter(r => roles.includes(r.roleName))
    .map(r => r.id)
  roleDialog.value = true
}

async function handleAssignRoles() {
  await assignRoles(currentUser.value.id, selectedRoles.value)
  ElMessage.success('角色已更新')
  roleDialog.value = false
}

onMounted(fetchData)
</script>
