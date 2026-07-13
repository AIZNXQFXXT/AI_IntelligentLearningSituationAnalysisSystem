import request from '@/utils/request'
import type { ApiResponse, PageResult, Teacher, TeacherForm } from '@/types'

export function getTeacherPage(params: { page?: number; size?: number; keyword?: string }) {
  return request.get<ApiResponse<PageResult<Teacher>>>('/teachers', { params }).then(res => res.data.data)
}

export function createTeacher(data: TeacherForm) {
  return request.post<ApiResponse<void>>('/teachers', data).then(res => res.data)
}

export function updateTeacher(id: number, data: TeacherForm) {
  return request.put<ApiResponse<void>>(`/teachers/${id}`, data).then(res => res.data)
}

export function deleteTeacher(id: number) {
  return request.delete<ApiResponse<void>>(`/teachers/${id}`).then(res => res.data)
}

export function batchImportTeacher(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<{ taskId: string }>>('/teachers/batch', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(res => res.data.data)
}

export function updateTeacherStatus(id: number, status: number) {
  return request.patch<ApiResponse<void>>(`/teachers/${id}/status`, { status }).then(res => res.data)
}
