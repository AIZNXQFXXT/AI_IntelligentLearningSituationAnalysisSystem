import request from '@/utils/request'
import type { ApiResponse, PageResult, RiskWarning, RiskWarningHandleForm } from '@/types'

export function getRiskWarningPage(params: { page?: number; size?: number; semester?: string; riskLevel?: string; handleStatus?: string }) {
  return request.get<ApiResponse<PageResult<RiskWarning>>>('/risk-warnings', { params }).then(res => res.data.data)
}

export function handleRiskWarning(id: number, data: RiskWarningHandleForm) {
  return request.post<ApiResponse<void>>(`/risk-warnings/${id}/handle`, data).then(res => res.data)
}

export function getMyWarnings() {
  return request.get<ApiResponse<RiskWarning[]>>('/risk-warnings/my').then(res => res.data.data)
}
