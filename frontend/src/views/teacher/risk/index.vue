<template>
  <el-card>
    <template #header>风险预警管理</template>
    <el-form :inline="true" style="margin-bottom:16px">
      <el-form-item label="学期">
        <el-select v-model="query.semester" placeholder="学期" clearable style="width:150px">
          <el-option label="2025-1" value="2025-1" />
          <el-option label="2024-2" value="2024-2" />
        </el-select>
      </el-form-item>
      <el-form-item label="风险等级">
        <el-select v-model="query.riskLevel" placeholder="等级" clearable style="width:120px">
          <el-option label="高风险" value="HIGH" />
          <el-option label="中风险" value="MEDIUM" />
          <el-option label="低风险" value="LOW" />
        </el-select>
      </el-form-item>
      <el-form-item label="处理状态">
        <el-select v-model="query.handleStatus" placeholder="状态" clearable style="width:120px">
          <el-option label="未处理" value="UNHANDLED" />
          <el-option label="已处理" value="HANDLED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadData">查询</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="studentId" label="学生ID" width="100" />
      <el-table-column prop="semester" label="学期" width="120" />
      <el-table-column prop="riskLevel" label="风险等级" width="100">
        <template #default="{ row }">
          <el-tag :type="row.riskLevel === 'HIGH' ? 'danger' : row.riskLevel === 'MEDIUM' ? 'warning' : 'success'">{{ row.riskLevel }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="riskReason" label="风险原因" show-overflow-tooltip />
      <el-table-column prop="handleStatus" label="处理状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.handleStatus === 'HANDLED' ? 'success' : 'danger'">{{ row.handleStatus === 'HANDLED' ? '已处理' : '未处理' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="row.handleStatus !== 'HANDLED'" size="small" type="warning" @click="openHandle(row)">处理</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      style="margin-top:16px;justify-content:flex-end"
      v-model:current-page="page" v-model:page-size="pageSize" :total="total"
      :page-sizes="[10,20,50]" layout="total, sizes, prev, pager, next"
      @size-change="loadData" @current-change="loadData"
    />
    <el-dialog v-model="handleVisible" title="处理预警" width="500px">
      <el-form label-width="80px">
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.handleRemark" type="textarea" :rows="3" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>
<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getRiskWarningPage, handleRiskWarning } from '@/api/riskWarning'
import type { RiskWarning, RiskWarningHandleForm } from '@/types'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref<RiskWarning[]>([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const query = reactive({ semester: '', riskLevel: '', handleStatus: '' })
const handleVisible = ref(false)
const handleId = ref(0)
const handleForm = reactive<RiskWarningHandleForm>({ handleRemark: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await getRiskWarningPage({ page: page.value, size: pageSize.value, ...query })
    tableData.value = res.records
    total.value = res.total
  } finally { loading.value = false }
}

function openHandle(row: RiskWarning) {
  handleId.value = row.id
  handleForm.handleRemark = ''
  handleVisible.value = true
}

async function submitHandle() {
  await handleRiskWarning(handleId.value, handleForm)
  ElMessage.success('处理成功')
  handleVisible.value = false
  loadData()
}

onMounted(loadData)
</script>
