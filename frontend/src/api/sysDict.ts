import request from '@/utils/request'
import type { ApiResponse, SysDict, SysDictForm } from '@/types'

export function getDictList(typeCode: string) {
  return request.get<ApiResponse<SysDict[]>>('/dicts', { params: { typeCode } }).then(res => res.data.data)
}

export function createDict(data: SysDictForm) {
  return request.post<ApiResponse<void>>('/dicts', data).then(res => res.data)
}

export function updateDict(id: number, data: SysDictForm) {
  return request.put<ApiResponse<void>>(`/dicts/${id}`, data).then(res => res.data)
}

export function deleteDict(id: number) {
  return request.delete<ApiResponse<void>>(`/dicts/${id}`).then(res => res.data)
}
