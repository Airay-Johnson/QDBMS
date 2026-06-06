<template>
  <div class="page-container">
    <div class="page-header">
      <h2>问卷管理</h2>
      <el-button type="primary" @click="$router.push('/questionnaires/create')">
        <el-icon><Plus /></el-icon> 创建问卷
      </el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索问卷标题" clearable style="width:280px"
        @keydown.enter="fetchData" />
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width:140px"
        @change="fetchData">
        <el-option label="全部" :value="null" />
        <el-option label="草稿" :value="0" />
        <el-option label="已发布" :value="1" />
        <el-option label="已关闭" :value="2" />
      </el-select>
    </div>

    <el-card>
      <el-table :data="filteredList" stripe v-loading="loading" empty-text="暂无问卷">
        <el-table-column prop="title" label="问卷标题" min-width="260" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" @click="$router.push(`/questionnaires/${row.id}/edit`)">编辑</el-button>
            <el-button v-if="row.status === 0" size="small" text type="success" @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.status === 1" size="small" text type="warning" @click="handleClose(row)">关闭</el-button>
            <el-button size="small" text @click="$router.push(`/questionnaires/${row.id}/stats`)">统计</el-button>
            <el-button size="small" text type="info" @click="copySurveyLink(row)">链接</el-button>
            <el-popconfirm title="确定删除？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button size="small" text type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { listQuestionnaires, publishQuestionnaire, closeQuestionnaire, deleteQuestionnaire } from '@/api/questionnaire'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const questionnaires = ref([])
const keyword = ref('')
const statusFilter = ref(null)

const filteredList = computed(() => {
  let list = questionnaires.value
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    list = list.filter(q => q.title?.toLowerCase().includes(kw))
  }
  if (statusFilter.value !== null && statusFilter.value !== '') {
    list = list.filter(q => q.status === statusFilter.value)
  }
  return list
})

function statusType(s) {
  return s === 0 ? 'info' : s === 1 ? 'success' : 'warning'
}

function statusText(s) {
  return s === 0 ? '草稿' : s === 1 ? '已发布' : '已关闭'
}

async function fetchData() {
  loading.value = true
  try {
    questionnaires.value = await listQuestionnaires() || []
  } finally {
    loading.value = false
  }
}

async function handlePublish(row) {
  await publishQuestionnaire(row.id)
  ElMessage.success('发布成功')
  fetchData()
}

async function handleClose(row) {
  await ElMessageBox.confirm('确定关闭？', '确认', { type: 'warning' })
  await closeQuestionnaire(row.id)
  ElMessage.success('已关闭')
  fetchData()
}

async function handleDelete(row) {
  await deleteQuestionnaire(row.id)
  ElMessage.success('已删除')
  fetchData()
}

function copySurveyLink(row) {
  if (row.status !== 1) {
    ElMessage.warning('请先发布问卷')
    return
  }
  const link = `${window.location.origin}/#/survey/${row.id}`
  navigator.clipboard.writeText(link).then(() => {
    ElMessage.success('问卷链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.info(`问卷链接: ${link}`)
  })
}

onMounted(fetchData)
</script>
