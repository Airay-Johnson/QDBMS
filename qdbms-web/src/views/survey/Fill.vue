<template>
  <div class="survey-page">
    <div v-if="loading" style="text-align:center;padding:60px">
      <el-icon class="is-loading" :size="32"><Loading /></el-icon>
      <p style="margin-top:12px;color:#999">加载问卷中...</p>
    </div>

    <template v-else-if="submitted">
      <div class="survey-card" style="text-align:center">
        <el-result icon="success" title="提交成功" sub-title="感谢您的参与！您的答卷已提交。">
          <template #extra>
            <el-button type="primary" @click="resetForm">再次填写</el-button>
          </template>
        </el-result>
      </div>
    </template>

    <template v-else>
      <div class="survey-card">
        <h1 class="survey-title">{{ questionnaire.title }}</h1>
        <p v-if="questionnaire.description" class="survey-desc">{{ questionnaire.description }}</p>

        <div v-for="(q, idx) in questions" :key="q.id" class="question-block">
          <div class="question-label">
            {{ idx + 1 }}. {{ q.questionText }}
            <span v-if="q.isRequired" class="required">*</span>
          </div>

          <!-- 单选题 -->
          <template v-if="q.type === 'single'">
            <el-radio-group v-model="answers[q.id]" class="option-group">
              <el-radio v-for="opt in getOptions(q.options)" :key="opt.label"
                :value="opt.value" class="option-item">
                {{ opt.label }}. {{ opt.value }}
              </el-radio>
            </el-radio-group>
          </template>

          <!-- 多选题 -->
          <template v-if="q.type === 'multiple'">
            <el-checkbox-group v-model="answers[q.id]" class="option-group">
              <el-checkbox v-for="opt in getOptions(q.options)" :key="opt.label"
                :label="opt.value" :value="opt.value" class="option-item">
                {{ opt.label }}. {{ opt.value }}
              </el-checkbox>
            </el-checkbox-group>
          </template>

          <!-- 文本题 -->
          <template v-if="q.type === 'text'">
            <el-input v-model="answers[q.id]" type="textarea" :rows="3"
              placeholder="请输入您的回答" />
          </template>
        </div>

        <div style="text-align:center;margin-top:32px">
          <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
            提交答卷
          </el-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getQuestionnaire, listQuestions } from '@/api/questionnaire'
import { submitResponse } from '@/api/response'
import { ElMessage } from 'element-plus'

const route = useRoute()
const loading = ref(true)
const submitting = ref(false)
const submitted = ref(false)
const questionnaire = ref({})
const questions = ref([])
const answers = reactive({})

async function loadSurvey() {
  try {
    questionnaire.value = await getQuestionnaire(route.params.id)
    if (questionnaire.value.status !== 1) {
      ElMessage.error('该问卷未发布或已关闭')
      return
    }
    questions.value = await listQuestions(route.params.id) || []
    // 初始化答案
    questions.value.forEach(q => {
      if (q.type === 'multiple') {
        answers[q.id] = []
      } else {
        answers[q.id] = ''
      }
    })
  } catch {
    ElMessage.error('加载问卷失败')
  } finally {
    loading.value = false
  }
}

function getOptions(optionsJson) {
  try {
    return JSON.parse(optionsJson || '[]')
  } catch {
    return []
  }
}

async function handleSubmit() {
  // 检查必答题
  for (const q of questions.value) {
    if (!q.isRequired) continue
    const ans = answers[q.id]
    if (!ans || (Array.isArray(ans) && ans.length === 0)) {
      ElMessage.warning(`第 ${questions.value.indexOf(q) + 1} 题为必答题`)
      return
    }
  }

  const payload = questions.value.map(q => {
    const ans = answers[q.id]
    return {
      questionId: q.id,
      answerText: q.type === 'text' ? ans : null,
      answerOptions: (q.type === 'single' || q.type === 'multiple') ? JSON.stringify(ans) : null
    }
  })

  submitting.value = true
  try {
    await submitResponse(questionnaire.value.id, payload)
    submitted.value = true
  } catch {}
  submitting.value = false
}

function resetForm() {
  submitted.value = false
  questions.value.forEach(q => {
    if (q.type === 'multiple') {
      answers[q.id] = []
    } else {
      answers[q.id] = ''
    }
  })
}

onMounted(loadSurvey)
</script>

<style scoped>
.survey-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e8edf5 0%, #f0f2f5 100%);
  padding: 40px 20px;
  display: flex;
  justify-content: center;
}
.survey-card {
  width: 100%;
  max-width: 760px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 48px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
}
.survey-title {
  font-size: 24px;
  text-align: center;
  margin-bottom: 8px;
}
.survey-desc {
  text-align: center;
  color: #999;
  margin-bottom: 28px;
}
.question-block {
  margin-bottom: 28px;
}
.question-label {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #1a1a1a;
}
.required {
  color: #f56c6c;
}
.option-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.option-item {
  padding: 10px 14px;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  transition: all 0.2s;
  margin: 0 !important;
}
.option-item:hover {
  border-color: #409eff;
  background: #ecf5ff;
}
</style>
