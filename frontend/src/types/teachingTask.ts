export interface TeachingTask {
  id: number
  teacherId: number
  classId: number
  courseId: number
  semester: string
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}

export interface TeachingTaskForm {
  id?: number
  teacherId: number | null
  classId: number | null
  courseId: number | null
  semester: string
}
