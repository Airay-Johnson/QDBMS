<template>
  <div class="page-container">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑问卷' : '创建问卷' }}</h2>
      <div>
        <el-button @click="$router.back()">返回</el-button>
        <el-button type="primary" @click="handleSave">保存问卷</el-button>
      </div>
    </div>

    <!-- 问卷基本信息 -->
    <el-card style="margin-bottom:20px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入问卷标题" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="问卷说明（可选）" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 题目列表 -->
    <el-card v-if="qId" style="margin-bottom:20px">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>题目列表（{{ questions.length }}）</span>
          <div>
            <el-button type="primary" @click="addQuestion('single')">添加单选题</el-button>
            <el-button type="primary" @click="addQuestion('multiple')">添加多选题</el-button>
            <el-button type="primary" @click="addQuestion('text')">添加文本题</el-button>
          </div>
        </div>
      </template>

      <div v-if="questions.length === 0" style="text-align:center;padding:40px;color:#999">
        还没有题目，请点击上方按钮添加
      </div>

      <draggable v-model="questions" item-key="id" handle=".drag-handle" @end="handleSort"
        :animation="200" ghost-class="ghost">
        <template #item="{ element, index }">
          <div class="question-item">
            <div class="question-header">
              <el-icon class="drag-handle" :size="18"><Rank /></el-icon>
              <span class="q-number">{{ index + 1 }}.</span>
              <el-tag size="small" :type="typeTag(element.type)">
                {{ typeLabel(element.type) }}
              </el-tag>
              <span class="q-text">{{ element.questionText || '未填写题目' }}</span>
              <div style="margin-left:auto;display:flex;gap:8px">
                <el-button size="small" text @click="editQuestion(element)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-popconfirm title="确定删除？" @confirm="handleDeleteQuestion(element)">
                  <template #reference>
                    <el-button size="small" text type="danger">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </div>
          </div>
        </template>
      </draggable>
    </el-card>

    <!-- 题目编辑弹窗 -->
    <el-dialog v-model="questionDialog" :title="editingQ.id ? '编辑题目' : '添加题目'" width="640px" destroy-on-close>
      <el-form :model="qForm" label-width="80px">
        <el-form-item label="题目内容">
          <el-input v-model="qForm.questionText" type="textarea" :rows="2" placeholder="请输入题目" />
        </el-form-item>
        <el-form-item label="题型">
          <el-tag>{{ typeLabel(qForm.type) }}</el-tag>
        </el-form-item>
        <el-form-item label="是否必答">
          <el-switch v-model="qForm.isRequired" />
        </el-form-item>
        <el-form-item v-if="qForm.type !== 'text'" label="选项">
          <div v-for="(opt, idx) in qForm.options" :key="idx" class="option-row">
            <el-input v-model="opt.label" placeholder="选项标签" style="width:100px" />
            <el-input v-model="opt.value" placeholder="选项内容" style="width:260px" />
            <el-button v-if="qForm.options.length > 2" type="danger" text @click="removeOption(idx)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <el-button type="primary" text @click="addOption" style="margin-top:8px">
            + 添加选项
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="questionDialog = false">取消</el-button>
        <el-button type="primary" @click="handleSaveQuestion">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createQuestionnaire, getQuestionnaire, updateQuestionnaire,
  createQuestion, listQuestions, updateQuestion, deleteQuestion, sortQuestions
} from '@/api/questionnaire'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)

const form = reactive({ title: '', description: '' })
const formRef = ref(null)
const formRules = {
  title: [{ required: true, message: '请输入问卷标题', trigger: 'blur' }]
}

const qId = ref(null)
const questions = ref([])
const questionDialog = ref(false)
const editingQ = ref({})
const qForm = reactive({
  questionText: '',
  type: 'single',
  isRequired: true,
  options: [{ label: 'A', value: '' }, { label: 'B', value: '' }]
})

function typeLabel(t) {
  return t === 'single' ? '单选' : t === 'multiple' ? '多选' : '文本'
}
function typeTag(t) {
  return t === 'single' ? '' : t === 'multiple' ? 'success' : 'info'
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  try {
    if (isEdit.value) {
      await updateQuestionnaire(qId.value, form)
    } else {
      const q = await createQuestionnaire(form)
      qId.value = q.id
    }
    ElMessage.success('问卷已保存')
    if (!isEdit.value) {
      router.replace(`/questionnaires/${qId.value}/edit`)
    }
  } catch {}
}

async function loadQuestionnaire() {
  if (!isEdit.value) {
    qId.value = null
    return
  }
  const q = await getQuestionnaire(route.params.id)
  qId.value = q.id
  form.title = q.title
  form.description = q.description || ''
  await loadQuestions()
}

async function loadQuestions() {
  questions.value = await listQuestions(qId.value) || []
}

function addQuestion(type) {
  editingQ.value = {}
  qForm.questionText = ''
  qForm.type = type
  qForm.isRequired = true
  qForm.options = [{ label: 'A', value: '' }, { label: 'B', value: '' }]
  questionDialog.value = true
}

function editQuestion(q) {
  editingQ.value = q
  qForm.questionText = q.questionText
  qForm.type = q.type
  qForm.isRequired = q.isRequired
  try {
    const opts = JSON.parse(q.options || '[]')
    qForm.options = opts.length > 0 ? opts : [{ label: 'A', value: '' }, { label: 'B', value: '' }]
  } catch {
    qForm.options = [{ label: 'A', value: '' }, { label: 'B', value: '' }]
  }
  questionDialog.value = true
}

function addOption() {
  const next = String.fromCharCode(65 + qForm.options.length)
  qForm.options.push({ label: next, value: '' })
}

function removeOption(idx) {
  qForm.options.splice(idx, 1)
}

async function handleSaveQuestion() {
  const data = {
    questionText: qForm.questionText,
    type: qForm.type,
    isRequired: qForm.isRequired,
    options: qForm.type !== 'text' ? JSON.stringify(qForm.options) : null
  }

  try {
    if (editingQ.value.id) {
      await updateQuestion(editingQ.value.id, data)
    } else {
      await createQuestion(qId.value, data)
    }
    ElMessage.success('题目已保存')
    questionDialog.value = false
    await loadQuestions()
  } catch {}
}

async function handleDeleteQuestion(q) {
  await deleteQuestion(q.id)
  ElMessage.success('已删除')
  await loadQuestions()
}

async function handleSort() {
  const ids = questions.value.map(q => q.id)
  await sortQuestions(ids)
}

onMounted(loadQuestionnaire)
</script>

<style scoped>
.question-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  padding: 14px 16px;
  margin-bottom: 10px;
  background: #fff;
  transition: box-shadow 0.2s;
}
.question-item:hover {
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.question-header {
  display: flex;
  align-items: center;
  gap: 10px;
}
.q-number {
  font-weight: 600;
  color: #409eff;
  min-width: 24px;
}
.q-text {
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.drag-handle {
  cursor: grab;
  color: #999;
}
.drag-handle:active {
  cursor: grabbing;
}
.option-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}
.ghost {
  opacity: 0.4;
}
</style>
