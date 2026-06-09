<template>
  <el-container class="app-wrapper">
    <el-aside :width="sidebarWidth" class="sidebar">
      <div class="logo">
        <el-icon :size="22"><Monitor /></el-icon>
        <span v-if="!collapsed">QDBMS</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        router
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#fff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/questionnaires">
          <el-icon><Document /></el-icon>
          <span>问卷管理</span>
        </el-menu-item>
        <el-menu-item v-permission="'ADMIN'" index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="navbar">
        <div class="nav-left">
          <el-icon class="collapse-btn" @click="collapsed = !collapsed" :size="20">
            <Fold v-if="!collapsed" /><Expand v-else />
          </el-icon>
          <span class="page-title">{{ currentTitle }}</span>
        </div>
        <div class="nav-right">
          <span class="username">
            {{ userStore.userInfo?.username }}
            <el-tag v-if="userStore.isAdmin" size="small" type="danger" style="margin-left:6px">管理员</el-tag>
          </span>
          <el-button type="danger" text @click="handleLogout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)

const sidebarWidth = computed(() => collapsed.value ? '64px' : '220px')
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta?.title || '')
const isAdmin = computed(() => userStore.isAdmin)

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-wrapper {
  min-height: 100vh;
}
.sidebar {
  background: #001529;
  overflow: hidden;
  transition: width 0.3s;
}
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.navbar {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  height: 56px;
}
.nav-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.collapse-btn {
  cursor: pointer;
  color: #666;
}
.page-title {
  font-size: 16px;
  font-weight: 500;
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.username {
  display: flex;
  align-items: center;
  color: #666;
  font-size: 14px;
}
.main-content {
  background: #f0f2f5;
  min-height: calc(100vh - 56px);
}
</style>
