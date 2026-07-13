export interface Student {
  id: number
  userId: number
  studentNo: string
  name: string
  gender: string | null
  classId: number
  enrollYear: string | null
  status: number
  phone: string | null
  guardianPhone: string | null
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}

export interface StudentForm {
  id?: number
  userId?: number
  studentNo: string
  name: string
  gender: string | null
  classId: number | null
  enrollYear: string | null
  status: number
  phone: string | null
  guardianPhone: string | null
  username?: string
  password?: string
}
