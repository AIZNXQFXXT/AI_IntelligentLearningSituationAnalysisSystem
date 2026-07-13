import request from '@/utils/request'
import type { ApiResponse, LoginForm, LoginResult } from '@/types'

export function login(data: LoginForm) {
  return request.post<ApiResponse<LoginResult>>('/auth/login', data).then(res => res.data.data)
}
