<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>AI学情诊断</span>
        <div style="display:flex;gap:12px">
          <el-select v-model="semester" placeholder="学期" style="width:150px">
            <el-option label="2025-1" value="2025-1" />
            <el-option label="2024-2" value="2024-2" />
          </el-select>
          <el-select v-model="studentId" placeholder="选择学生" filterable style="width:180px">
            <el-option v-for="s in students" :key="s.id" :label="s.studentNo + ' ' + s.name" :value="s.id" />
          </el-select>
          <el-button type="primary" :loading="generating" @click="handleGenerate">生成诊断</el-button>
        </div>
      </div>
    </template>
    <el-table :data="diagnoses" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="studentId" label="学生ID" width="100" />
      <el-table-column prop="semester" label="学期" width="120" />
      <el-table-column prop="riskLevel" label="风险等级" width="100">
        <template #default="{ row }">
          <el-tag :type="row.riskLevel === 'HIGH' ? 'danger' : row.riskLevel === 'MEDIUM' ? 'warning' : 'success'">{{ row.riskLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="diagnosisText" label="诊断摘要" show-overflow-tooltip />
      <el-table-column prop="aiModel" label="AI模型" width="120" />
      <el-table-column prop="tokensUsed" label="Token消耗" width="100" />
      <el-table-column prop="createdAt" label="生成时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="detailVisible" title="诊断报告详情" width="700px">
      <el-descriptions :column="1" border v-if="currentDiagnosis">
        <el-descriptions-item label="诊断内容">{{ currentDiagnosis.diagnosisText }}</el-descriptions-item>
        <el-descriptions-item label="优势学科">{{ currentDiagnosis.strengths }}</el-descriptions-item>
        <el-descriptions-item label="薄弱学科">{{ currentDiagnosis.weaknesses }}</el-descriptions-item>
        <el-descriptions-item label="趋势分析">{{ currentDiagnosis.trendAnalysis }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">{{ currentDiagnosis.riskLevel }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-card>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDiagnosisPage, generateDiagnosis } from '@/api/diagnosis'
import { getStudentPage } from '@/api/student'
import type { AiDiagnosis, Student } from '@/types'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const generating = ref(false)
const diagnoses = ref<AiDiagnosis[]>([])
const students = ref<Student[]>([])
const semester = ref('2025-1')
const studentId = ref<number>()
const detailVisible = ref(false)
const currentDiagnosis = ref<AiDiagnosis | null>(null)

async function loadData() {
  loading.value = true
  try {
    const res = await getDiagnosisPage({ page: 1, size: 100, semester: semester.value })
    diagnoses.value = res.records
  } finally { loading.value = false }
}

async function loadStudents() {
  const res = await getStudentPage({ page: 1, size: 500 })
  students.value = res.records
}

async function handleGenerate() {
  if (!studentId.value) { ElMessage.warning('请选择学生'); return }
  generating.value = true
  try {
    await generateDiagnosis({ studentId: studentId.value, semester: semester.value })
    ElMessage.success('诊断生成成功')
    loadData()
  } finally { generating.value = false }
}

function showDetail(row: AiDiagnosis) {
  currentDiagnosis.value = row
  detailVisible.value = true
}

onMounted(() => { loadData(); loadStudents() })
</script>
