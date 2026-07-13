import request from '@/utils/request'
import type { ApiResponse, PageResult, ClassInfo, ClassInfoForm } from '@/types'

export function getClassPage(params: { page?: number; size?: number; keyword?: string }) {
  return request.get<ApiResponse<PageResult<ClassInfo>>>('/classes', { params }).then(res => res.data.data)
}

export function getAllClasses() {
  return request.get<ApiResponse<ClassInfo[]>>('/classes/list').then(res => res.data.data)
}

export function createClass(data: ClassInfoForm) {
  return request.post<ApiResponse<void>>('/classes', data).then(res => res.data)
}

export function updateClass(id: number, data: ClassInfoForm) {
  return request.put<ApiResponse<void>>(`/classes/${id}`, data).then(res => res.data)
}

export function deleteClass(id: number) {
  return request.delete<ApiResponse<void>>(`/classes/${id}`).then(res => res.data)
}
