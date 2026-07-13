import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi } from '@/api/auth'
import type { LoginForm } from '@/types'
import { getToken, setToken, removeToken, getUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const role = ref<string>(getUserInfo()?.role || '')
  const username = ref<string>(getUserInfo()?.username || '')
  const userId = ref<number>(getUserInfo()?.userId || 0)

  const isLoggedIn = computed(() => !!token.value)

  async function login(form: LoginForm) {
    const data = await loginApi(form)
    token.value = data.token
    role.value = data.role
    username.value = data.username
    userId.value = data.userId
    setToken(data.token)
    setUserInfo({ token: data.token, role: data.role, username: data.username, userId: data.userId })
    return data
  }

  function logout() {
    token.value = ''
    role.value = ''
    username.value = ''
    userId.value = 0
    removeToken()
    removeUserInfo()
  }

  return { token, role, username, userId, isLoggedIn, login, logout }
})
