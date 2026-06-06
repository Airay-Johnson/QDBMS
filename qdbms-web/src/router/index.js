import { createRouter, createWebHashHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

function isLoggedIn() {
  return !!getToken()
}

function getRoles() {
  try {
    const token = getToken()
    if (!token) return []
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.roles || []
  } catch {
    return []
  }
}

function hasRole(role) {
  return getRoles().includes(role)
}

const routes = [
  {
    path: '/',
    component: () => import('@/components/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'questionnaires',
        name: 'QuestionnaireList',
        component: () => import('@/views/questionnaire/List.vue'),
        meta: { title: '问卷管理' }
      },
      {
        path: 'questionnaires/create',
        name: 'QuestionnaireCreate',
        component: () => import('@/views/questionnaire/Editor.vue'),
        meta: { title: '创建问卷' }
      },
      {
        path: 'questionnaires/:id/edit',
        name: 'QuestionnaireEdit',
        component: () => import('@/views/questionnaire/Editor.vue'),
        meta: { title: '编辑问卷' }
      },
      {
        path: 'questionnaires/:id/stats',
        name: 'QuestionnaireStats',
        component: () => import('@/views/analysis/Stats.vue'),
        meta: { title: '数据统计' }
      },
      {
        path: 'admin/users',
        name: 'UserManagement',
        component: () => import('@/views/admin/Users.vue'),
        meta: { title: '用户管理', roles: ['ADMIN'] }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { guest: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { guest: true }
  },
  {
    path: '/survey/:id',
    name: 'SurveyFill',
    component: () => import('@/views/survey/Fill.vue'),
    meta: { public: true }
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  // 公开页面直接放行
  if (to.meta.public) {
    next()
    return
  }

  // 已登录用户访问登录/注册页 → 跳到工作台
  if (to.meta.guest && isLoggedIn()) {
    next('/dashboard')
    return
  }

  // 未登录用户 → 跳到登录页
  if (!to.meta.guest && !isLoggedIn()) {
    next('/login')
    return
  }

  // 角色检查
  if (to.meta.roles) {
    const required = to.meta.roles
    const allowed = required.some(r => hasRole(r))
    if (!allowed) {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
