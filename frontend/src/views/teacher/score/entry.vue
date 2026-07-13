<template>
  <el-card>
    <template #header>成绩录入</template>
    <el-form :inline="true" style="margin-bottom:16px">
      <el-form-item label="考试">
        <el-select v-model="examId" placeholder="选择考试" style="width:200px">
          <el-option v-for="e in exams" :key="e.id" :label="e.name" :value="e.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="课程">
        <el-select v-model="courseId" placeholder="选择课程" style="width:160px">
          <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="班级">
        <el-select v-model="classId" placeholder="选择班级" style="width:180px">
          <el-option v-for="cl in classes" :key="cl.id" :label="cl.grade + ' ' + cl.className" :value="cl.id" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="loadStudents">加载学生</el-button>
      </el-form-item>
    </el-form>
    <el-table :data="studentScores" v-loading="loading" border stripe>
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column label="平时分" width="150">
        <template #default="{ row }">
          <el-input-number v-model="row.regularScore" :min="0" :max="100" :precision="1" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="卷面分" width="150">
        <template #default="{ row }">
          <el-input-number v-model="row.examScore" :min="0" :max="100" :precision="1" size="small" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button size="small" type="primary" :loading="row._submitting" @click="submitScore(row)">提交</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getExamPage } from '@/api/exam'
import { getAllCourses } from '@/api/course'
import { getAllClasses } from '@/api/classInfo'
import { getStudentPage } from '@/api/student'
import { createScore } from '@/api/score'
import type { Exam, Course, ClassInfo, Student } from '@/types'
import { ElMessage } from 'element-plus'

const exams = ref<Exam[]>([])
const courses = ref<Course[]>([])
const classes = ref<ClassInfo[]>([])
const examId = ref<number>()
const courseId = ref<number>()
const classId = ref<number>()
const loading = ref(false)
const studentScores = ref<(Student & { regularScore: number; examScore: number; _submitting: boolean })[]>([])

async function loadOptions() {
  const [examRes, courseRes, classRes] = await Promise.all([
    getExamPage({ page: 1, size: 100 }), getAllCourses(), getAllClasses()
  ])
  exams.value = examRes.records
  courses.value = courseRes
  classes.value = classRes
}

async function loadStudents() {
  if (!classId.value) { ElMessage.warning('请选择班级'); return }
  loading.value = true
  try {
    const res = await getStudentPage({ page: 1, size: 200, classId: classId.value })
    studentScores.value = res.records.map(s => ({ ...s, regularScore: 0, examScore: 0, _submitting: false }))
  } finally { loading.value = false }
}

async function submitScore(row: any) {
  if (!examId.value || !courseId.value) { ElMessage.warning('请先选择考试和课程'); return }
  row._submitting = true
  try {
    await createScore({ studentId: row.id, examId: examId.value, courseId: courseId.value, regularScore: row.regularScore, examScore: row.examScore })
    ElMessage.success(row.name + ' 成绩录入成功')
  } finally { row._submitting = false }
}

onMounted(loadOptions)
</script>
