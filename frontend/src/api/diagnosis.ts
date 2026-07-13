import request from '@/utils/request'
import type { ApiResponse, PageResult, AiDiagnosis, AiDiagnosisGenerateForm } from '@/types'

export function getDiagnosisPage(params: { page?: number; size?: number; studentId?: number; semester?: string }) {
  return request.get<ApiResponse<PageResult<AiDiagnosis>>>('/diagnoses', { params }).then(res => res.data.data)
}

export function generateDiagnosis(data: AiDiagnosisGenerateForm) {
  return request.post<ApiResponse<AiDiagnosis>>('/diagnoses', data).then(res => res.data.data)
}

export function getDiagnosis(id: number) {
  return request.get<ApiResponse<AiDiagnosis>>(`/diagnoses/${id}`).then(res => res.data.data)
}
