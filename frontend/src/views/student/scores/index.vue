<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>成绩查询</span>
        <el-select v-model="semester" placeholder="选择学期" style="width:150px" @change="loadData">
          <el-option label="2025-1" value="2025-1" />
          <el-option label="2024-2" value="2024-2" />
          <el-option label="2024-1" value="2024-1" />
        </el-select>
      </div>
    </template>
    <el-table :data="scores" v-loading="loading" border stripe>
      <el-table-column prop="courseId" label="课程ID" width="100" />
      <el-table-column prop="regularScore" label="平时分" width="100" />
      <el-table-column prop="examScore" label="卷面分" width="100" />
      <el-table-column prop="finalScore" label="最终成绩" width="100">
        <template #default="{ row }">
          <span style="font-weight:600">{{ row.finalScore }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="rankClass" label="班级排名" width="100" />
      <el-table-column prop="rankGrade" label="年级排名" width="100" />
      <el-table-column prop="auditStatus" label="审核状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.auditStatus === 'APPROVED' ? 'success' : 'info'">{{ row.auditStatus }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && scores.length === 0" description="暂无成绩数据" />
  </el-card>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getScorePage } from '@/api/score'
import type { Score } from '@/types'

const loading = ref(false)
const scores = ref<Score[]>([])
const semester = ref('2025-1')

async function loadData() {
  loading.value = true
  try {
    const res = await getScorePage({ page: 1, size: 200 })
    scores.value = res.records
  } finally { loading.value = false }
}

onMounted(loadData)
</script>
