<template>
  <el-card>
    <template #header>批量导入成绩</template>
    <el-form :inline="true" style="margin-bottom:20px">
      <el-form-item label="考试">
        <el-select v-model="examId" placeholder="选择考试" style="width:200px">
          <el-option v-for="e in exams" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="班级">
        <el-select v-model="classId" placeholder="选择班级" style="width:180px">
          <el-option v-for="c in classes" :key="c.id" :label="c.grade + ' ' + c.className" :value="c.id" />
        </el-select>
      </el-form-item>
    </el-form>
    <el-steps :active="step" finish-status="success" style="margin-bottom:30px">
      <el-step title="下载模板" description="下载成绩录入模板" />
      <el-step title="填写数据" description="按模板格式填写成绩" />
      <el-step title="上传文件" description="上传填好的Excel文件" />
    </el-steps>
    <div style="display:flex;gap:20px">
      <el-card shadow="never" style="flex:1">
        <div style="text-align:center">
          <p>下载导入模板</p>
          <el-button type="primary" @click="downloadTemplate">下载模板</el-button>
        </div>
      </el-card>
      <el-card shadow="never" style="flex:1">
        <div style="text-align:center">
          <p>上传成绩文件</p>
          <el-upload :show-file-list="false" accept=".xlsx,.xls" :before-upload="handleImport">
            <el-button type="success" :disabled="!examId || !classId">上传文件</el-button>
          </el-upload>
        </div>
      </el-card>
    </div>
    <div v-if="taskId" style="margin-top:20px">
      <el-alert title="导入任务已提交" type="success" show-icon :description="'任务ID: ' + taskId" />
    </div>
  </el-card>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getExamPage } from '@/api/exam'
import { getAllClasses } from '@/api/classInfo'
import { batchImportScore } from '@/api/score'
import type { Exam, ClassInfo } from '@/types'
import { ElMessage } from 'element-plus'
import * as XLSX from 'xlsx'

const exams = ref<Exam[]>([])
const classes = ref<ClassInfo[]>([])
const examId = ref<number>()
const classId = ref<number>()
const step = ref(0)
const taskId = ref('')

async function loadOptions() {
  const [examRes, classRes] = await Promise.all([getExamPage({ page: 1, size: 100 }), getAllClasses()])
  exams.value = examRes.records
  classes.value = classRes
}

function downloadTemplate() {
  const ws = XLSX.utils.aoa_to_sheet([['学号', '姓名', '平时分', '卷面分']])
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '成绩导入模板')
  XLSX.writeFile(wb, '成绩导入模板.xlsx')
  step.value = 1
  ElMessage.success('模板下载成功')
}

async function handleImport(file: File) {
  if (!examId.value || !classId.value) { ElMessage.warning('请先选择考试和班级'); return false }
  try {
    const res = await batchImportScore(file, examId.value, classId.value)
    taskId.value = res.taskId
    step.value = 2
    ElMessage.success('导入任务已提交')
  } catch { /* handled */ }
  return false
}

onMounted(loadOptions)
</script>
