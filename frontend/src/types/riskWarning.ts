export interface RiskWarning {
  id: number
  studentId: number
  semester: string
  riskLevel: string
  riskReason: string | null
  aiAnalysis: string | null
  handleStatus: string
  handlerId: number
  handleRemark: string | null
  handleAt: string | null
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}

export interface RiskWarningHandleForm {
  handleRemark: string
}
