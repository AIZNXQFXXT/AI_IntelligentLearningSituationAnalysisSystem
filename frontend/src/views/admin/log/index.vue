<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>操作日志</span>
        <div style="display:flex;gap:12px">
          <el-input v-model="query.username" placeholder="用户名" clearable style="width:150px" />
          <el-input v-model="query.operation" placeholder="操作类型" clearable style="width:150px" />
          <el-date-picker v-model="dateRange" type="daterange" value-format="YYYY-MM-DD" start-placeholder="开始日期" end-placeholder="结束日期" style="width:260px" />
          <el-button type="primary" icon="Search" @click="loadData">查询</el-button>
        </div>
      </div>
    </template>

    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户" width="100" />
      <el-table-column prop="operation" label="操作" width="120" />
      <el-table-column prop="targetType" label="目标类型" width="100" />
      <el-table-column prop="targetId" label="目标ID" width="80" />
      <el-table-column prop="detail" label="详情" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="130" />
      <el-table-column prop="resultStatus" label="结果" width="80" />
      <el-table-column prop="durationMs" label="耗时(ms)" width="90" />
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
import { getOperationLogPage } from '@/api/operationLog'
import type { OperationLog } from '@/types'

const loading = ref(false)
const tableData = ref<OperationLog[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const dateRange = ref<[string, string] | null>(null)
const query = reactive({ username: '', operation: '', startDate: '', endDate: '' })

async function loadData() {
  loading.value = true
  if (dateRange.value) {
    query.startDate = dateRange.value[0]
    query.endDate = dateRange.value[1]
  } else {
    query.startDate = ''
    query.endDate = ''
  }
  try {
    const res = await getOperationLogPage({ page: page.value, size: pageSize.value, ...query })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
