export interface Teacher {
  id: number
  userId: number
  teacherNo: string
  name: string
  title: string | null
  subject: string | null
  education: string | null
  department: string | null
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}

export interface TeacherForm {
  id?: number
  userId?: number
  teacherNo: string
  name: string
  title: string | null
  subject: string | null
  education: string | null
  department: string | null
  username?: string
  password?: string
  phone?: string
}
