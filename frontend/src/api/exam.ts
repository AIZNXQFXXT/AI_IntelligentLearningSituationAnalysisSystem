import request from '@/utils/request'
import type { ApiResponse, PageResult, Exam, ExamForm } from '@/types'

export function getExamPage(params: { page?: number; size?: number; semester?: string; type?: string }) {
  return request.get<ApiResponse<PageResult<Exam>>>('/exams', { params }).then(res => res.data.data)
}

export function createExam(data: ExamForm) {
  return request.post<ApiResponse<void>>('/exams', data).then(res => res.data)
}

export function updateExam(id: number, data: ExamForm) {
  return request.put<ApiResponse<void>>(`/exams/${id}`, data).then(res => res.data)
}

export function deleteExam(id: number) {
  return request.delete<ApiResponse<void>>(`/exams/${id}`).then(res => res.data)
}

export function archiveExam(id: number) {
  return request.patch<ApiResponse<void>>(`/exams/${id}/archive`).then(res => res.data)
}
