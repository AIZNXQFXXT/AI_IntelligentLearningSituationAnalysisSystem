<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>教学任务管理</span>
          <div style="display:flex;gap:12px">
            <el-input v-model="semesterFilter" placeholder="学期" clearable style="width:150px" @clear="loadData" @keyup.enter="loadData" />
            <el-button type="primary" icon="Plus" @click="openDialog()">新增任务</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="teacherId" label="教师ID" width="100" />
        <el-table-column prop="classId" label="班级ID" width="100" />
        <el-table-column prop="courseId" label="课程ID" width="100" />
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑任务' : '新增任务'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="教师ID" prop="teacherId">
          <el-input-number v-model="form.teacherId" :min="0" />
        </el-form-item>
        <el-form-item label="班级ID" prop="classId">
          <el-input-number v-model="form.classId" :min="0" />
        </el-form-item>
        <el-form-item label="课程ID" prop="courseId">
          <el-input-number v-model="form.courseId" :min="0" />
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="form.semester" placeholder="如: 2025-1" />
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
import { getTeachingTaskPage, createTeachingTask, updateTeachingTask, deleteTeachingTask } from '@/api/teachingTask'
import type { TeachingTask, TeachingTaskForm } from '@/types'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<TeachingTask[]>([])
const semesterFilter = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<TeachingTaskForm>({ teacherId: null, classId: null, courseId: null, semester: '' })
const rules: FormRules = {
  teacherId: [{ required: true, message: '请输入教师ID', trigger: 'blur' }],
  classId: [{ required: true, message: '请输入班级ID', trigger: 'blur' }],
  courseId: [{ required: true, message: '请输入课程ID', trigger: 'blur' }],
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getTeachingTaskPage({ page: page.value, size: pageSize.value, semester: semesterFilter.value })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function openDialog(row?: TeachingTask) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, { id: row.id, teacherId: row.teacherId, classId: row.classId, courseId: row.courseId, semester: row.semester })
  } else {
    Object.assign(form, { id: undefined, teacherId: null, classId: null, courseId: null, semester: '' })
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
        await updateTeachingTask(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await createTeachingTask(form)
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
  await deleteTeachingTask(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
