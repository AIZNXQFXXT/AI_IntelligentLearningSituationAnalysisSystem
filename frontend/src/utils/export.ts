import * as XLSX from 'xlsx'
import { ElMessage } from 'element-plus'

export interface ExportColumn {
  header: string
  key: string
  width?: number
}

export function exportToExcel(data: Record<string, any>[], columns: ExportColumn[], filename: string): void {
  try {
    const headers = columns.map((c) => c.header)
    const keys = columns.map((c) => c.key)
    const widths = columns.map((c) => ({ wch: c.width || 15 }))

    const sheetData = data.map((row) => keys.map((k) => row[k] ?? ''))

    const ws = XLSX.utils.aoa_to_sheet([headers, ...sheetData])
    ws['!cols'] = widths

    const wb = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(wb, ws, 'Sheet1')
    XLSX.writeFile(wb, `${filename}.xlsx`)
    ElMessage.success('导出成功')
  } catch {
    ElMessage.error('导出失败')
  }
}
