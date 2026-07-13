export interface SysConfig {
  id: number
  configKey: string
  configValue: string
  description: string | null
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}

export interface SysDict {
  id: number
  typeCode: string
  itemCode: string
  itemValue: string
  sortOrder: number
  status: number
  createdAt: string
  updatedAt: string
  isDeleted: boolean
}

export interface SysDictForm {
  id?: number
  typeCode: string
  itemCode: string
  itemValue: string
  sortOrder: number
  status: number
}
