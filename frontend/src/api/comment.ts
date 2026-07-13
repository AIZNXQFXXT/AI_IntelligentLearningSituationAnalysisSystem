import request from '@/utils/request'
import type { ApiResponse, PageResult, AiComment, AiCommentVersion, AiCommentGenerateForm } from '@/types'

export function getCommentPage(params: { page?: number; size?: number; studentId?: number; semester?: string }) {
  return request.get<ApiResponse<PageResult<AiComment>>>('/comments', { params }).then(res => res.data.data)
}

export function batchGenerateComments(data: AiCommentGenerateForm) {
  return request.post<ApiResponse<{ taskId: string }>>('/comments/batch', data).then(res => res.data.data)
}

export function updateComment(id: number, data: { content: string }) {
  return request.put<ApiResponse<void>>(`/comments/${id}`, data).then(res => res.data)
}

export function getCommentVersions(id: number) {
  return request.get<ApiResponse<AiCommentVersion[]>>(`/comments/${id}/versions`).then(res => res.data.data)
}
