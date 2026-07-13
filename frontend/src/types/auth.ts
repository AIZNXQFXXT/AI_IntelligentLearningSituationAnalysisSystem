export interface LoginForm {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  role: 'ADMIN' | 'TEACHER' | 'STUDENT'
  username: string
  userId: number
}
