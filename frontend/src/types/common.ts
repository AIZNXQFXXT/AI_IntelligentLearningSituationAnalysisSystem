export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T = any> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}

export interface PageParams {
  page?: number
  size?: number
  keyword?: string
}
