/**
 * v-permission 权限指令
 * 用法:
 *   v-permission="'ADMIN'"                          — 仅管理员可见
 *   v-permission="['ADMIN', 'ANALYST']"             — 任一角色可见
 *   v-permission:hide                                 — 不可见时隐藏（默认就是隐藏）
 */
import { useUserStore } from '@/stores/user'

export default {
  mounted(el, binding) {
    const userStore = useUserStore()
    const required = binding.value
    if (!required) return

    const roles = Array.isArray(required) ? required : [required]
    const hasAccess = roles.some(r => userStore.roles.includes(r))

    if (!hasAccess) {
      el.parentNode?.removeChild(el)
    }
  }
}
