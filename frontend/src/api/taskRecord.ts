import request from '@/utils/request'
import type { ApiResponse, TaskRecord } from '@/types'

export function getTaskStatus(id: number) {
  return request.get<ApiResponse<TaskRecord>>(`/tasks/${id}`).then(res => res.data.data)
}
