export interface AiComment {
  id: number
  studentId: number
  teacherId: number
  semester: string
  content: string
  isTeacherEdited: number
  status: string
  generatedBy: string
  tokensUsed: number
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}

export interface AiCommentVersion {
  id: number
  commentId: number
  versionNo: number
  content: string
  source: string
  tokensUsed: number
  createdAt: string
  isDeleted: boolean
}

export interface AiCommentGenerateForm {
  studentIds: number[]
  semester: string
}
