<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>考试批次管理</span>
          <div style="display:flex;gap:12px">
            <el-input v-model="semesterFilter" placeholder="学期" clearable style="width:150px" @clear="loadData" @keyup.enter="loadData" />
            <el-button type="primary" icon="Plus" @click="openDialog()">新增考试</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="考试名称" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ examTypeMap[row.type] || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column prop="classId" label="班级ID" width="80" />
        <el-table-column prop="examDate" label="考试日期" width="120" />
        <el-table-column prop="isArchived" label="归档" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isArchived ? 'success' : 'info'">{{ row.isArchived ? '已归档' : '未归档' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
            <el-button v-if="!row.isArchived" size="small" type="warning" @click="handleArchive(row.id)">归档</el-button>
            <el-popconfirm title="确认删除?" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button size="small" type="danger">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑考试' : '新增考试'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="考试名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type">
            <el-option label="月考" value="MOCK" />
            <el-option label="期中" value="MIDTERM" />
            <el-option label="期末" value="FINAL" />
            <el-option label="补考" value="RETEST" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="form.semester" placeholder="如: 2025-1" />
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-input-number v-model="form.classId" :min="0" />
        </el-form-item>
        <el-form-item label="考试日期">
          <el-date-picker v-model="form.examDate" type="date" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getExamPage, createExam, updateExam, deleteExam, archiveExam } from '@/api/exam'
import type { Exam, ExamForm } from '@/types'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

const examTypeMap: Record<string, string> = { MOCK: '月考', MIDTERM: '期中', FINAL: '期末', RETEST: '补考' }

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<Exam[]>([])
const semesterFilter = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<ExamForm>({ name: '', type: 'MOCK', semester: '', classId: null, examDate: null })
const rules: FormRules = {
  name: [{ required: true, message: '请输入考试名', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getExamPage({ page: page.value, size: pageSize.value, semester: semesterFilter.value })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function openDialog(row?: Exam) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, { id: row.id, name: row.name, type: row.type, semester: row.semester, classId: row.classId, examDate: row.examDate })
  } else {
    Object.assign(form, { id: undefined, name: '', type: 'MOCK', semester: '', classId: null, examDate: null })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      if (isEdit.value && form.id) {
        await updateExam(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await createExam(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      loadData()
    } finally {
      submitting.value = false
    }
  })
}

async function handleDelete(id: number) {
  await deleteExam(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleArchive(id: number) {
  await archiveExam(id)
  ElMessage.success('归档成功')
  loadData()
}

onMounted(loadData)
</script>
