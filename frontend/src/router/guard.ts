import type { Router } from 'vue-router'
import { getToken, getUserInfo } from '@/utils/auth'

const roleHomeMap: Record<string, string> = {
  ADMIN: '/admin/dashboard',
  TEACHER: '/teacher/dashboard',
  STUDENT: '/student/dashboard'
}

export function setupRouterGuard(router: Router) {
  router.beforeEach((to, _from, next) => {
    const token = getToken()
    const userInfo = getUserInfo()

    if (to.path === '/login') {
      if (token) {
        next(roleHomeMap[userInfo?.role || 'ADMIN'] || '/login')
      } else {
        next()
      }
      return
    }

    if (!token) {
      next('/login')
      return
    }

    const requiredRole = to.meta.role as string | undefined
    const userRole = userInfo?.role

    if (requiredRole && userRole !== requiredRole) {
      next(roleHomeMap[userRole || 'ADMIN'] || '/login')
      return
    }

    next()
  })
}
