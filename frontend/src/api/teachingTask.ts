import request from '@/utils/request'
import type { ApiResponse, PageResult, TeachingTask, TeachingTaskForm } from '@/types'

export function getTeachingTaskPage(params: { page?: number; size?: number; semester?: string; teacherId?: number }) {
  return request.get<ApiResponse<PageResult<TeachingTask>>>('/teaching-tasks', { params }).then(res => res.data.data)
}

export function createTeachingTask(data: TeachingTaskForm) {
  return request.post<ApiResponse<void>>('/teaching-tasks', data).then(res => res.data)
}

export function updateTeachingTask(id: number, data: TeachingTaskForm) {
  return request.put<ApiResponse<void>>(`/teaching-tasks/${id}`, data).then(res => res.data)
}

export function deleteTeachingTask(id: number) {
  return request.delete<ApiResponse<void>>(`/teaching-tasks/${id}`).then(res => res.data)
}
