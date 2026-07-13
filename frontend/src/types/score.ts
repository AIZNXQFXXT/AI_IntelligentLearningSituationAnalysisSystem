export interface Score {
  id: number
  studentId: number
  examId: number
  courseId: number
  regularScore: number
  examScore: number
  finalScore: number
  rankClass: number
  rankGrade: number
  isAbsent: number
  isCheat: number
  auditStatus: string
  enteredBy: number
  reason: string | null
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}

export interface ScoreForm {
  id?: number
  studentId: number
  examId: number
  courseId: number
  regularScore: number | null
  examScore: number | null
  isAbsent?: number
  isCheat?: number
  reason?: string
}

export interface ScoreBatchParams {
  examId: number
  classId: number
  file: File
}
