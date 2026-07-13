<template>
  <el-card>
    <template #header>系统配置</template>
    <el-table :data="configList" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="configKey" label="配置键" />
      <el-table-column prop="configValue" label="配置值">
        <template #default="{ row }">
          <el-input v-model="row.configValue" size="small" />
        </template>
      </el-table-column>
      <el-table-column prop="description" label="描述" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="handleUpdate(row)">保存</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getConfigList, updateConfig } from '@/api/sysConfig'
import type { SysConfig } from '@/types'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const configList = ref<SysConfig[]>([])

async function loadData() {
  loading.value = true
  try {
    configList.value = await getConfigList()
  } finally {
    loading.value = false
  }
}

async function handleUpdate(row: SysConfig) {
  await updateConfig(row.id, { configValue: row.configValue })
  ElMessage.success('保存成功')
}

onMounted(loadData)
</script>
