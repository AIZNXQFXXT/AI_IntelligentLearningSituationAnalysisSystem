export interface ClassInfo {
  id: number
  grade: string
  className: string
  headTeacherId: number
  studentCount: number
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}

export interface ClassInfoForm {
  id?: number
  grade: string
  className: string
  headTeacherId: number | null
}
