<template>
  <el-card>
    <template #header>本班学生</template>
    <el-table :data="tableData" v-loading="loading" border stripe>
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="gender" label="性别" width="80" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="enrollYear" label="入学年份" width="100" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" @click="showDetail(row)">查看画像</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="detailVisible" title="学生画像" width="700px">
      <el-descriptions :column="2" border v-if="currentStudent">
        <el-descriptions-item label="学号">{{ currentStudent.studentNo }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ currentStudent.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ currentStudent.gender }}</el-descriptions-item>
        <el-descriptions-item label="班级ID">{{ currentStudent.classId }}</el-descriptions-item>
        <el-descriptions-item label="入学年份">{{ currentStudent.enrollYear }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ currentStudent.phone }}</el-descriptions-item>
        <el-descriptions-item label="监护人电话">{{ currentStudent.guardianPhone }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </el-card>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getStudentPage } from '@/api/student'
import type { Student } from '@/types'

const loading = ref(false)
const tableData = ref<Student[]>([])
const detailVisible = ref(false)
const currentStudent = ref<Student | null>(null)

async function loadData() {
  loading.value = true
  try {
    const res = await getStudentPage({ page: 1, size: 200 })
    tableData.value = res.records
  } finally {
    loading.value = false
  }
}

function showDetail(row: Student) {
  currentStudent.value = row
  detailVisible.value = true
}

onMounted(loadData)
</script>
