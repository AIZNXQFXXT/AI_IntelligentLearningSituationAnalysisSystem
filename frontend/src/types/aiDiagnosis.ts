export interface AiDiagnosis {
  id: number
  studentId: number
  semester: string
  diagnosisText: string | null
  strengths: string | null
  weaknesses: string | null
  trendAnalysis: string | null
  riskLevel: string | null
  tokensUsed: number
  cost: number
  durationMs: number
  aiModel: string
  promptTemplate: string
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}

export interface AiDiagnosisGenerateForm {
  studentId: number
  semester: string
}
