import request from '@/utils/request'
import type { ApiResponse, PageResult, Score, ScoreForm } from '@/types'

export function getScorePage(params: { page?: number; size?: number; examId?: number; courseId?: number; studentId?: number }) {
  return request.get<ApiResponse<PageResult<Score>>>('/scores', { params }).then(res => res.data.data)
}

export function createScore(data: ScoreForm) {
  return request.post<ApiResponse<void>>('/scores', data).then(res => res.data)
}

export function updateScore(id: number, data: ScoreForm & { reason: string }) {
  return request.put<ApiResponse<void>>(`/scores/${id}`, data).then(res => res.data)
}

export function batchImportScore(file: File, examId: number, classId: number) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('examId', String(examId))
  formData.append('classId', String(classId))
  return request.post<ApiResponse<{ taskId: string }>>('/scores/batch', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(res => res.data.data)
}
