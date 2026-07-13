import request from '@/utils/request'
import type { ApiResponse, PageResult, Student, StudentForm } from '@/types'

export function getStudentPage(params: { page?: number; size?: number; keyword?: string; classId?: number }) {
  return request.get<ApiResponse<PageResult<Student>>>('/students', { params }).then(res => res.data.data)
}

export function createStudent(data: StudentForm) {
  return request.post<ApiResponse<void>>('/students', data).then(res => res.data)
}

export function updateStudent(id: number, data: StudentForm) {
  return request.put<ApiResponse<void>>(`/students/${id}`, data).then(res => res.data)
}

export function deleteStudent(id: number) {
  return request.delete<ApiResponse<void>>(`/students/${id}`).then(res => res.data)
}

export function batchImportStudent(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<ApiResponse<{ taskId: string }>>('/students/batch', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(res => res.data.data)
}

export function updateStudentStatus(id: number, status: number) {
  return request.patch<ApiResponse<void>>(`/students/${id}/status`, { status }).then(res => res.data)
}
