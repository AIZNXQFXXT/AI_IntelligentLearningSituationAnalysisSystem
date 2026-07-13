<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>班级管理</span>
          <div style="display:flex;gap:12px">
            <el-input v-model="keyword" placeholder="搜索班级/年级" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
            <el-button type="primary" icon="Plus" @click="openDialog()">新增班级</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="grade" label="年级" width="120" />
        <el-table-column prop="className" label="班级名称" />
        <el-table-column prop="headTeacherId" label="班主任ID" width="100" />
        <el-table-column prop="studentCount" label="学生人数" width="100" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑班级' : '新增班级'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="年级" prop="grade">
          <el-input v-model="form.grade" placeholder="如: 2024级" />
        </el-form-item>
        <el-form-item label="班级名" prop="className">
          <el-input v-model="form.className" placeholder="如: 计算机1班" />
        </el-form-item>
        <el-form-item label="班主任ID" prop="headTeacherId">
          <el-input-number v-model="form.headTeacherId" :min="0" />
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
import { getClassPage, createClass, updateClass, deleteClass } from '@/api/classInfo'
import type { ClassInfo, ClassInfoForm } from '@/types'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref<ClassInfo[]>([])
const keyword = ref('')
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const form = reactive<ClassInfoForm>({ grade: '', className: '', headTeacherId: null })
const rules: FormRules = {
  grade: [{ required: true, message: '请输入年级', trigger: 'blur' }],
  className: [{ required: true, message: '请输入班级名', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getClassPage({ page: page.value, size: pageSize.value, keyword: keyword.value })
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function openDialog(row?: ClassInfo) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, { id: row.id, grade: row.grade, className: row.className, headTeacherId: row.headTeacherId })
  } else {
    Object.assign(form, { id: undefined, grade: '', className: '', headTeacherId: null })
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
        await updateClass(form.id, form)
        ElMessage.success('更新成功')
      } else {
        await createClass(form)
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
  await deleteClass(id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(loadData)
</script>
