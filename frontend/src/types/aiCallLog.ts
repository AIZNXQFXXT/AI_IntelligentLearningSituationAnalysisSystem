export interface AiCallLog {
  id: number
  callerId: number
  callerRole: string | null
  functionName: string | null
  aiModel: string | null
  requestBody: string | null
  responseBody: string | null
  httpStatus: number | null
  tokensInput: number | null
  tokensOutput: number | null
  tokensTotal: number | null
  estimatedCost: number | null
  durationMs: number
  success: number
  errorMessage: string | null
  promptTemplate: string | null
  createdAt: string
  isDeleted: boolean
}
