<template>
  <el-card>
    <template #header>AI学习建议</template>
    <el-empty v-if="!loading && !diagnosis" description="暂无学习建议，请等待教师生成AI诊断" />
    <div v-else>
      <el-descriptions :column="1" border v-if="diagnosis">
        <el-descriptions-item label="优势学科">{{ diagnosis.strengths || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="薄弱学科">{{ diagnosis.weaknesses || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="趋势分析">{{ diagnosis.trendAnalysis || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="诊断详情">{{ diagnosis.diagnosisText || '暂无' }}</el-descriptions-item>
      </el-descriptions>
    </div>
  </el-card>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getDiagnosisPage } from '@/api/diagnosis'
import { useUserStore } from '@/stores/user'
import type { AiDiagnosis } from '@/types'

const userStore = useUserStore()
const loading = ref(false)
const diagnosis = ref<AiDiagnosis | null>(null)

onMounted(async () => {
  loading.value = true
  try {
    const res = await getDiagnosisPage({ page: 1, size: 1, studentId: userStore.userId })
    if (res.records.length > 0) diagnosis.value = res.records[0]
  } finally { loading.value = false }
})
</script>
