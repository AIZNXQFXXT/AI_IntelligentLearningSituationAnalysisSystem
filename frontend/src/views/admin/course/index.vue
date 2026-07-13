<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>课程管理</span>
          <div style="display:flex;gap:12px">
            <el-select v-model="typeFilter" placeholder="课程类型" clearable style="width:130px" @change="loadData">
              <el-option label="必修" value="REQUIRED" />
              <el-option label="选修" value="ELECTIVE" />
              <el-option label="专业" value="MAJOR" />
            </el-select>
            <el-input v-model="keyword" placeholder="搜索课程名" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
            <el-button type="primary" icon="Plus" @click="openDialog()">新增课程</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="课程名称" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ typeMap[row.type] || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课程' : '新增课程'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="课程名" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type">
            <el-option label="必修" value="REQUIRED" />
            <el-option label="选修" value="ELECTIVE" />
            <el-option label="专业" value="MAJOR" />
          </el-select>
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="form.credit" :min="0" :max="10" :step="0.5" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import { getCoursePage, createCourse, updateCourse, deleteCourse } from '@/api/course'
import type { Course, CourseForm } from '@/types'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

const typeMap: Record<string, string> = { REQUIRED: '必修', ELECTIVE: '选修', MAJOR: '专业' }

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<Course[]>([])
const keyword = ref('')
const typeFilter = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<CourseForm>({ name: '', type: 'REQUIRED', credit: 1, description: null, status: 1 })
const rules: FormRules = {
  name: [{ required: true, message: '请输入课程名', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  credit: [{ required: true, message: '请输入学分', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getCoursePage({ page: page.value, size: pageSize.value, keyword: keyword.value, type: typeFilter.value })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function openDialog(row?: Course) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, { id: row.id, name: row.name, type: row.type, credit: row.credit, description: row.description, status: row.status })
  } else {
    Object.assign(form, { id: undefined, name: '', type: 'REQUIRED', credit: 1, description: null, status: 1 })
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
        await updateCourse(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await createCourse(form)
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
  await deleteCourse(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
