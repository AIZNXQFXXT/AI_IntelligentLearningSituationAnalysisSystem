export interface Exam {
  id: number
  name: string
  type: string
  semester: string
  classId: number
  examDate: string | null
  isArchived: number
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}

export interface ExamForm {
  id?: number
  name: string
  type: string
  semester: string
  classId: number | null
  examDate: string | null
}
