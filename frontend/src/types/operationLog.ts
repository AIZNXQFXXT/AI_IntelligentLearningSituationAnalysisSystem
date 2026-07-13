export interface OperationLog {
  id: number
  username: string
  operatorId: number
  operation: string
  targetType: string | null
  targetId: number | null
  detail: string | null
  oldData: string | null
  newData: string | null
  ip: string | null
  userAgent: string | null
  durationMs: number
  resultStatus: string
  failReason: string | null
  createdAt: string
  isDeleted: boolean
}
