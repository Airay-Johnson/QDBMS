import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, removeToken, setUser, removeUser, getUser } from '@/utils/auth'
import { login as loginApi, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(getUser() || null)

  // 从 JWT 解析角色
  const roles = computed(() => {
    if (!token.value) return []
    try {
      const payload = JSON.parse(atob(token.value.split('.')[1]))
      return payload.roles || []
    } catch {
      return []
    }
  })

  const isAdmin = computed(() => roles.value.includes('ADMIN'))

  // 检查是否有指定权限
  function hasRole(role) {
    return roles.value.includes(role)
  }

  function hasAnyRole(...roleList) {
    return roleList.some(r => roles.value.includes(r))
  }

  async function login(credentials) {
    const data = await loginApi(credentials)
    token.value = data.token
    setToken(data.token)
    try {
      const user = await getCurrentUser()
      userInfo.value = user
      setUser(user)
    } catch {
      // 即使获取用户详情失败，只要 JWT 有效就算已登录
    }
    return data
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    removeToken()
    removeUser()
  }

  async function fetchUserInfo() {
    try {
      const user = await getCurrentUser()
      userInfo.value = user
      setUser(user)
    } catch {
      logout()
    }
  }

  return { token, userInfo, roles, isAdmin, hasRole, hasAnyRole, login, logout, fetchUserInfo }
})
