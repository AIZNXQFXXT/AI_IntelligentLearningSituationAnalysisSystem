<template>
  <el-card>
    <template #header>AI诊断报告</template>
    <el-table :data="diagnoses" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="semester" label="学期" width="120" />
      <el-table-column prop="riskLevel" label="风险等级" width="100">
        <template #default="{ row }">
          <el-tag :type="row.riskLevel === 'HIGH' ? 'danger' : row.riskLevel === 'MEDIUM' ? 'warning' : 'success'">{{ row.riskLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="diagnosisText" label="诊断摘要" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="生成时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">查看详情</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && diagnoses.length === 0" description="暂无诊断报告" />
    <el-dialog v-model="detailVisible" title="诊断报告详情" width="700px">
      <el-descriptions :column="1" border v-if="current">
        <el-descriptions-item label="诊断内容">{{ current.diagnosisText }}</el-descriptions-item>
        <el-descriptions-item label="优势学科">{{ current.strengths }}</el-descriptions-item>
        <el-descriptions-item label="薄弱学科">{{ current.weaknesses }}</el-descriptions-item>
        <el-descriptions-item label="趋势分析">{{ current.trendAnalysis }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-card>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDiagnosisPage } from '@/api/diagnosis'
import { useUserStore } from '@/stores/user'
import type { AiDiagnosis } from '@/types'

const userStore = useUserStore()
const loading = ref(false)
const diagnoses = ref<AiDiagnosis[]>([])
const detailVisible = ref(false)
const current = ref<AiDiagnosis | null>(null)

async function loadData() {
  loading.value = true
  try {
    const res = await getDiagnosisPage({ page: 1, size: 50, studentId: userStore.userId })
    diagnoses.value = res.records
  } finally { loading.value = false }
}

function showDetail(row: AiDiagnosis) { current.value = row; detailVisible.value = true }

onMounted(loadData)
</script>
