import request from '@/utils/request'
import type { ApiResponse, PageResult, OperationLog } from '@/types'

export function getOperationLogPage(params: { page?: number; size?: number; username?: string; operation?: string; startDate?: string; endDate?: string }) {
  return request.get<ApiResponse<PageResult<OperationLog>>>('/operation-logs', { params }).then(res => res.data.data)
}
