<template>
  <el-card v-loading="exporting">
    <template #header>报表导出</template>
    <el-form :inline="true" style="margin-bottom:20px">
      <el-form-item label="导出类型">
        <el-select v-model="exportType" style="width:200px">
          <el-option label="班级成绩表" value="scores" />
          <el-option label="评语汇总" value="comments" />
          <el-option label="高风险清单" value="risks" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="exportType === 'scores'" label="班级">
        <el-select v-model="classId" placeholder="选择班级" style="width:180px">
          <el-option v-for="c in classes" :key="c.id" :label="c.grade + ' ' + c.className" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="exportType === 'scores'" label="考试">
        <el-select v-model="examId" placeholder="选择考试" style="width:200px">
          <el-option v-for="e in exams" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="exportType === 'risks'" label="风险等级">
        <el-select v-model="riskLevel" placeholder="选择等级" clearable style="width:140px">
          <el-option label="高风险" value="HIGH" />
          <el-option label="中风险" value="MEDIUM" />
          <el-option label="低风险" value="LOW" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :disabled="exportType === 'scores' && (!classId || !examId)" @click="handleExport">导出Excel</el-button>
      </el-form-item>
    </el-form>
    <el-empty description="选择导出类型和范围，点击导出" />
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAllClasses } from '@/api/classInfo'
import { getExamPage } from '@/api/exam'
import { getScorePage } from '@/api/score'
import { getCommentPage } from '@/api/comment'
import { getRiskWarningPage } from '@/api/riskWarning'
import type { ClassInfo, Exam } from '@/types'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'

const classes = ref<ClassInfo[]>([])
const exams = ref<Exam[]>([])
const exportType = ref('scores')
const classId = ref<number>()
const examId = ref<number>()
const riskLevel = ref('')
const exporting = ref(false)

onMounted(async () => {
  const [classRes, examRes] = await Promise.all([getAllClasses(), getExamPage({ page: 1, size: 200 })])
  classes.value = classRes
  exams.value = examRes.records
})

function downloadExcel(data: any[][], filename: string, sheetName: string) {
  const ws = XLSX.utils.aoa_to_sheet(data)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, sheetName)
  XLSX.writeFile(wb, `${filename}.xlsx`)
}

async function handleExport() {
  exporting.value = true
  try {
    if (exportType.value === 'scores') {
      const res = await getScorePage({ page: 1, size: 1000, examId: examId.value, courseId: undefined })
      const rows = res.records.map(s => [
        s.studentId, s.regularScore, s.examScore, s.finalScore,
        s.rankClass, s.rankGrade, s.auditStatus
      ])
      const header = ['学生ID', '平时分', '卷面分', '最终成绩', '班级排名', '年级排名', '审核状态']
      downloadExcel([header, ...rows], '班级成绩表', '成绩')
      ElMessage.success('成绩表导出成功')
    } else if (exportType.value === 'comments') {
      const res = await getCommentPage({ page: 1, size: 1000 })
      const rows = res.records.map(c => [
        c.studentId, c.semester, c.content, c.isTeacherEdited ? '教师修改' : 'AI生成', c.createdAt
      ])
      const header = ['学生ID', '学期', '评语内容', '来源', '生成时间']
      downloadExcel([header, ...rows], '评语汇总', '评语')
      ElMessage.success('评语汇总导出成功')
    } else if (exportType.value === 'risks') {
      const res = await getRiskWarningPage({ page: 1, size: 1000, riskLevel: riskLevel.value || undefined })
      const rows = res.records.map(r => [
        r.studentId, r.semester, r.riskLevel, r.riskReason, r.handleStatus, r.handleRemark || ''
      ])
      const header = ['学生ID', '学期', '风险等级', '风险原因', '处理状态', '处理备注']
      downloadExcel([header, ...rows], '高风险清单', '预警')
      ElMessage.success('预警清单导出成功')
    }
  } catch {
    ElMessage.error('导出失败')
  } finally {
    exporting.value = false
  }
}
</script>
