<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>教师管理</span>
          <div style="display:flex;gap:12px">
            <el-input v-model="keyword" placeholder="搜索姓名/工号" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
            <el-upload ref="uploadRef" :show-file-list="false" accept=".xlsx,.xls" :before-upload="handleBatchImport">
              <el-button icon="Upload">批量导入</el-button>
            </el-upload>
            <el-button type="primary" icon="Plus" @click="openDialog()">新增教师</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="teacherNo" label="工号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="title" label="职称" width="120" />
        <el-table-column prop="subject" label="学科" width="120" />
        <el-table-column prop="department" label="院系" />
        <el-table-column prop="education" label="学历" width="100" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="openDialog(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑教师' : '新增教师'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="工号" prop="teacherNo">
          <el-input v-model="form.teacherNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="职称">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="学科">
          <el-input v-model="form.subject" />
        </el-form-item>
        <el-form-item label="学历">
          <el-input v-model="form.education" />
        </el-form-item>
        <el-form-item label="院系">
          <el-input v-model="form.department" />
        </el-form-item>
        <template v-if="!isEdit">
          <el-form-item label="登录账号" prop="username">
            <el-input v-model="form.username" />
          </el-form-item>
          <el-form-item label="初始密码" prop="password">
            <el-input v-model="form.password" type="password" />
          </el-form-item>
        </template>
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
import { getTeacherPage, createTeacher, updateTeacher, deleteTeacher, batchImportTeacher } from '@/api/teacher'
import type { Teacher, TeacherForm } from '@/types'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<Teacher[]>([])
const keyword = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<TeacherForm>({ teacherNo: '', name: '', title: null, subject: null, education: null, department: null, username: '', password: '' })
const rules: FormRules = {
  teacherNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getTeacherPage({ page: page.value, size: pageSize.value, keyword: keyword.value })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function openDialog(row?: Teacher) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, { id: row.id, teacherNo: row.teacherNo, name: row.name, title: row.title, subject: row.subject, education: row.education, department: row.department })
  } else {
    Object.assign(form, { id: undefined, teacherNo: '', name: '', title: null, subject: null, education: null, department: null, username: '', password: '' })
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
        await updateTeacher(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await createTeacher(form)
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
  await deleteTeacher(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchImport(file: File) {
  try {
    await batchImportTeacher(file)
    ElMessage.success('导入任务已提交')
    loadData()
  } catch { /* handled */ }
  return false
}

onMounted(loadData)
</script>
