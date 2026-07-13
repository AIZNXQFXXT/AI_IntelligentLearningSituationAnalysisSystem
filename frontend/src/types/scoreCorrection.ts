export interface ScoreCorrection {
  id: number
  scoreId: number
  oldFinalScore: number
  newFinalScore: number
  reason: string
  operatorId: number
  operatedAt: string
  createdAt: string
  isDeleted: boolean
}
