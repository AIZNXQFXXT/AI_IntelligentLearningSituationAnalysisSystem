export interface User {
  id: number
  username: string
  password: string
  role: string
  avatar: string | null
  phone: string | null
  status: number
  createdAt: string
  updatedAt: string
  isDeleted: number
}
