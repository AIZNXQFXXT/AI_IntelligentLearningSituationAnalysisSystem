export interface KnowledgePoint {
  id: number
  parentId: number
  name: string
  subjectType: string | null
  level: number
  sortOrder: number
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}
