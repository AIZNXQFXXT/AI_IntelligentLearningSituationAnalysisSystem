<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>AI调用日志</span>
        <div style="display:flex;gap:12px">
          <el-input v-model="query.functionName" placeholder="功能名" clearable style="width:150px" />
          <el-select v-model="query.success" placeholder="状态" clearable style="width:120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
          <el-button type="primary" icon="Search" @click="loadData">查询</el-button>
        </div>
      </div>
    </template>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="functionName" label="功能" width="120" />
      <el-table-column prop="aiModel" label="模型" width="100" />
      <el-table-column prop="callerRole" label="调用角色" width="100" />
      <el-table-column prop="httpStatus" label="HTTP状态" width="100" />
      <el-table-column prop="tokensInput" label="输入Token" width="100" />
      <el-table-column prop="tokensOutput" label="输出Token" width="100" />
      <el-table-column prop="tokensTotal" label="总Token" width="100" />
      <el-table-column prop="estimatedCost" label="费用($)" width="90" />
      <el-table-column prop="durationMs" label="耗时(ms)" width="90" />
      <el-table-column prop="success" label="结果" width="80">
        <template #default="{ row }">
          <el-tag :type="row.success ? 'success' : 'danger'">{{ row.success ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="errorMessage" label="错误信息" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="时间" width="180" />
    </el-table>

    <el-pagination
      style="margin-top:16px;justify-content:flex-end"
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10,20,50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadData"
      @current-change="loadData"
    />
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAiCallLogPage } from '@/api/aiCallLog'
import type { AiCallLog } from '@/types'

const loading = ref(false)
const tableData = ref<AiCallLog[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const query = reactive({ functionName: '', success: undefined as number | undefined })

async function loadData() {
  loading.value = true
  try {
    const res = await getAiCallLogPage({ page: page.value, size: pageSize.value, ...query })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
