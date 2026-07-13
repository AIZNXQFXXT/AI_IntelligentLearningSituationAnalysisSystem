export interface Course {
  id: number
  name: string
  type: string
  credit: number
  description: string | null
  status: number
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}

export interface CourseForm {
  id?: number
  name: string
  type: string
  credit: number
  description: string | null
  status: number
}
