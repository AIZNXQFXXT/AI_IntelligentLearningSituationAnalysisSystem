import request from '@/utils/request'
import type { ApiResponse, SysConfig } from '@/types'

export function getConfigList() {
  return request.get<ApiResponse<SysConfig[]>>('/configs').then(res => res.data.data)
}

export function updateConfig(id: number, data: { configValue: string }) {
  return request.put<ApiResponse<void>>(`/configs/${id}`, data).then(res => res.data)
}
