export interface TaskRecord {
  id: number
  taskType: string
  status: string
  progress: number
  currentCount: number
  totalCount: number
  fileUrl: string | null
  resultJson: string | null
  createdBy: number
  errorMessage: string | null
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}
