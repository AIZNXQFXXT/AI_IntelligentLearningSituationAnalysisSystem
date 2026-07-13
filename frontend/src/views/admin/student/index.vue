<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>学生管理</span>
          <div style="display:flex;gap:12px">
            <el-select v-model="classIdFilter" placeholder="筛选班级" clearable style="width:150px" @change="loadData">
              <el-option v-for="c in classes" :key="c.id" :label="`${c.grade} ${c.className}`" :value="c.id" />
            </el-select>
            <el-input v-model="keyword" placeholder="搜索姓名/学号" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
            <el-upload :show-file-list="false" accept=".xlsx,.xls" :before-upload="handleBatchImport">
              <el-button icon="Upload">批量导入</el-button>
            </el-upload>
            <el-button type="primary" icon="Plus" @click="openDialog()">新增学生</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80" />
        <el-table-column prop="classId" label="班级ID" width="80" />
        <el-table-column prop="enrollYear" label="入学年份" width="100" />
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑学生' : '新增学生'" width="550px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" placeholder="请选择">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="form.classId" placeholder="请选择班级">
            <el-option v-for="c in classes" :key="c.id" :label="`${c.grade} ${c.className}`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="入学年份">
          <el-input v-model="form.enrollYear" placeholder="如: 2024" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="监护人电话">
          <el-input v-model="form.guardianPhone" />
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
import { getStudentPage, createStudent, updateStudent, deleteStudent, batchImportStudent } from '@/api/student'
import { getAllClasses } from '@/api/classInfo'
import type { Student, StudentForm, ClassInfo } from '@/types'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<Student[]>([])
const classes = ref<ClassInfo[]>([])
const keyword = ref('')
const classIdFilter = ref<number | undefined>()
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<StudentForm>({ studentNo: '', name: '', gender: null, classId: null, enrollYear: null, status: 1, phone: null, guardianPhone: null, username: '', password: '' })
const rules: FormRules = {
  studentNo: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }],
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getStudentPage({ page: page.value, size: pageSize.value, keyword: keyword.value, classId: classIdFilter.value })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function loadClasses() {
  classes.value = await getAllClasses()
}

function openDialog(row?: Student) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, { id: row.id, studentNo: row.studentNo, name: row.name, gender: row.gender, classId: row.classId, enrollYear: row.enrollYear, status: row.status, phone: row.phone, guardianPhone: row.guardianPhone })
  } else {
    Object.assign(form, { id: undefined, studentNo: '', name: '', gender: null, classId: null, enrollYear: null, status: 1, phone: null, guardianPhone: null, username: '', password: '' })
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
        await updateStudent(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await createStudent(form)
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
  await deleteStudent(id)
  ElMessage.success('删除成功')
  loadData()
}

async function handleBatchImport(file: File) {
  try {
    await batchImportStudent(file)
    ElMessage.success('导入任务已提交')
    loadData()
  } catch { /* handled */ }
  return false
}

onMounted(() => {
  loadData()
  loadClasses()
})
</script>
