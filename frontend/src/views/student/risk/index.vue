<template>
  <el-card>
    <template #header>我的预警</template>
    <el-table :data="warnings" v-loading="loading" border stripe>
      <el-table-column prop="semester" label="学期" width="120" />
      <el-table-column prop="riskLevel" label="风险等级" width="100">
        <template #default="{ row }">
          <el-tag :type="row.riskLevel === 'HIGH' ? 'danger' : row.riskLevel === 'MEDIUM' ? 'warning' : 'success'">{{ row.riskLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="riskReason" label="预警原因" show-overflow-tooltip />
      <el-table-column prop="handleStatus" label="处理状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.handleStatus === 'HANDLED' ? 'success' : 'danger'">{{ row.handleStatus === 'HANDLED' ? '已处理' : '未处理' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handleRemark" label="教师跟进" show-overflow-tooltip />
    </el-table>
    <el-empty v-if="!loading && warnings.length === 0" description="暂无预警信息" />
  </el-card>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyWarnings } from '@/api/riskWarning'
import type { RiskWarning } from '@/types'

const loading = ref(false)
const warnings = ref<RiskWarning[]>([])

onMounted(async () => {
  loading.value = true
  try {
    warnings.value = await getMyWarnings()
  } finally { loading.value = false }
})
</script>
