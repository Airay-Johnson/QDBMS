<template>
  <router-view />
</template>

<script setup>
import { useUserStore } from '@/stores/user'
import { isLoggedIn } from '@/utils/auth'

const userStore = useUserStore()
if (isLoggedIn()) {
  userStore.fetchUserInfo()
}
</script>
