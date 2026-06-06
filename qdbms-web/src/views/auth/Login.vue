<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2 class="auth-title">QDBMS 登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large"
            show-password @keydown.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="submit-btn"
          @click="handleLogin">
          登 录
        </el-button>
      </el-form>
      <p class="switch-link">
        还没有账号？<router-link to="/register">立即注册</router-link>
      </p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.auth-card {
  width: 400px;
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  box-shadow: 0 8px 40px rgba(0,0,0,0.15);
}
.auth-title {
  text-align: center;
  font-size: 22px;
  margin-bottom: 30px;
  color: #1a1a1a;
}
.submit-btn {
  width: 100%;
  margin-top: 8px;
}
.switch-link {
  text-align: center;
  margin-top: 16px;
  color: #999;
  font-size: 14px;
}
.switch-link a {
  color: #409eff;
}
</style>
