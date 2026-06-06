<template>
  <div class="page-container">
    <h2 style="margin-bottom:24px">工作台</h2>
    <div class="stat-cards">
      <div class="stat-card">
        <div class="label">我的问卷</div>
        <div class="value">{{ stats.total }}</div>
      </div>
      <div class="stat-card">
        <div class="label">已发布</div>
        <div class="value" style="color:#67c23a">{{ stats.published }}</div>
      </div>
      <div class="stat-card">
        <div class="label">草稿</div>
        <div class="value" style="color:#e6a23c">{{ stats.draft }}</div>
      </div>
      <div class="stat-card">
        <div class="label">已关闭</div>
        <div class="value" style="color:#909399">{{ stats.closed }}</div>
      </div>
    </div>

    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>我的问卷</span>
          <el-button type="primary" @click="$router.push('/questionnaires/create')">
            创建问卷
          </el-button>
        </div>
      </template>
      <el-table :data="questionnaires" stripe v-loading="loading" empty-text="暂无问卷">
        <el-table-column prop="title" label="问卷标题" min-width="240" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="$router.push(`/questionnaires/${row.id}/edit`)">编辑</el-button>
            <el-button v-if="row.status === 0" size="small" type="success" @click="handlePublish(row)">发布</el-button>
            <el-button v-if="row.status === 1" size="small" type="warning" @click="handleClose(row)">关闭</el-button>
            <el-button size="small" @click="$router.push(`/questionnaires/${row.id}/stats`)">统计</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { listQuestionnaires, publishQuestionnaire, closeQuestionnaire } from '@/api/questionnaire'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const questionnaires = ref([])

const stats = computed(() => ({
  total: questionnaires.value.length,
  draft: questionnaires.value.filter(q => q.status === 0).length,
  published: questionnaires.value.filter(q => q.status === 1).length,
  closed: questionnaires.value.filter(q => q.status === 2).length
}))

function statusType(status) {
  return status === 0 ? 'info' : status === 1 ? 'success' : 'warning'
}

function statusText(status) {
  return status === 0 ? '草稿' : status === 1 ? '已发布' : '已关闭'
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
  await ElMessageBox.confirm('确定关闭该问卷？关闭后不再接受新答卷', '确认', { type: 'warning' })
  await closeQuestionnaire(row.id)
  ElMessage.success('已关闭')
  fetchData()
}

onMounted(fetchData)
</script>
