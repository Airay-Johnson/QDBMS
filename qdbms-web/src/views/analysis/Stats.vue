<template>
  <div class="page-container">
    <div class="page-header">
      <h2>数据统计 {{ stats.title ? '— ' + stats.title : '' }}</h2>
      <div>
        <el-button @click="$router.back()">返回</el-button>
        <el-button type="primary" @click="handleExport">导出 Excel</el-button>
      </div>
    </div>

    <div class="stat-cards" v-if="stats.totalResponses !== undefined">
      <div class="stat-card">
        <div class="label">总答卷数</div>
        <div class="value">{{ stats.totalResponses }}</div>
      </div>
      <div class="stat-card">
        <div class="label">题目数</div>
        <div class="value">{{ stats.totalQuestions }}</div>
      </div>
    </div>

    <el-card v-for="(qs, idx) in (stats.questionStats || [])" :key="idx" style="margin-bottom:20px">
      <template #header>
        <span style="font-weight:600">{{ idx + 1 }}. {{ qs.questionText }}</span>
        <el-tag size="small" style="margin-left:8px">{{ typeLabel(qs.type) }}</el-tag>
      </template>

      <!-- 单选统计 -->
      <template v-if="qs.type === 'single' && qs.distribution">
        <div v-for="d in qs.distribution" :key="d.option" class="stat-bar">
          <span class="bar-label">{{ d.option }}</span>
          <div class="bar-track">
            <div class="bar-fill" :style="{ width: d.percentage + '%' }"></div>
          </div>
          <span class="bar-count">{{ d.count }} ({{ d.percentage }}%)</span>
        </div>
      </template>

      <!-- 多选统计 -->
      <template v-if="qs.type === 'multiple' && qs.distribution">
        <div v-for="d in qs.distribution" :key="d.option" class="stat-bar">
          <span class="bar-label">{{ d.option }}</span>
          <div class="bar-track">
            <div class="bar-fill multi" :style="{ width: d.percentage + '%' }"></div>
          </div>
          <span class="bar-count">{{ d.count }} ({{ d.percentage }}%)</span>
        </div>
      </template>

      <!-- 文本答案 -->
      <template v-if="qs.type === 'text' && qs.answers">
        <div v-if="qs.answers.length === 0" style="color:#999;padding:16px">暂无答案</div>
        <div v-for="(ans, i) in qs.answers" :key="i" class="text-answer">
          <span class="text-num">{{ i + 1 }}</span>
          {{ ans }}
        </div>
      </template>
    </el-card>

    <div v-if="!stats.totalResponses && loaded" style="text-align:center;padding:60px;color:#999">
      暂无答卷数据
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getStats, getExportUrl } from '@/api/analysis'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const stats = ref({})
const loaded = ref(false)

function typeLabel(t) {
  return t === 'single' ? '单选题' : t === 'multiple' ? '多选题' : '文本题'
}

function handleExport() {
  const url = getExportUrl(route.params.id)
  const a = document.createElement('a')
  a.href = url
  // 添加 token 到 URL（后端通过 header 验证，这里用 query 参数方式）
  // 改用新窗口打开，浏览器自动带 cookie 不适用于 JWT
  // 这里改为使用 fetch + blob 下载
  fetch(url, { headers: { Authorization: `Bearer ${getToken()}` } })
    .then(res => {
      if (!res.ok) throw new Error('导出失败')
      return res.blob()
    })
    .then(blob => {
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `${stats.value.title || '问卷'}_答卷数据.xlsx`
      link.click()
      URL.revokeObjectURL(link.href)
      ElMessage.success('导出成功')
    })
    .catch(() => ElMessage.error('导出失败'))
}

onMounted(async () => {
  try {
    stats.value = await getStats(route.params.id) || {}
  } catch {}
  loaded.value = true
})
</script>

<style scoped>
.stat-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.bar-label {
  width: 120px;
  text-align: right;
  font-size: 13px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.bar-track {
  flex: 1;
  height: 24px;
  background: #f0f2f5;
  border-radius: 4px;
  overflow: hidden;
}
.bar-fill {
  height: 100%;
  background: #409eff;
  border-radius: 4px;
  transition: width 0.6s ease;
  min-width: 2px;
}
.bar-fill.multi {
  background: #67c23a;
}
.bar-count {
  width: 100px;
  font-size: 13px;
  color: #666;
}
.text-answer {
  padding: 10px 14px;
  border-bottom: 1px solid #f0f0f0;
  font-size: 14px;
  display: flex;
  gap: 10px;
}
.text-num {
  color: #999;
  min-width: 28px;
}
</style>
