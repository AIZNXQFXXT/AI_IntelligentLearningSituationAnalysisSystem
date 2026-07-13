import request from '@/utils/request'
import type { ApiResponse, PageResult, AiCallLog } from '@/types'

export function getAiCallLogPage(params: { page?: number; size?: number; functionName?: string; success?: number }) {
  return request.get<ApiResponse<PageResult<AiCallLog>>>('/ai-call-logs', { params }).then(res => res.data.data)
}
