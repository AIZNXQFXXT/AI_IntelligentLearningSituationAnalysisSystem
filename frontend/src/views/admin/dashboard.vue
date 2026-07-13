<template>
  <div class="admin-dashboard" v-loading="loading">
    <h2>管理员控制台</h2>
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-info">
              <div class="stat-title">{{ item.title }}</div>
              <div class="stat-value">{{ item.value }}</div>
            </div>
            <el-icon :size="40" :color="item.color"><component :is="item.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card>
          <template #header>快捷操作</template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/admin/classes')">班级管理</el-button>
            <el-button type="success" @click="$router.push('/admin/teachers')">教师管理</el-button>
            <el-button type="warning" @click="$router.push('/admin/students')">学生管理</el-button>
            <el-button type="info" @click="$router.push('/admin/teaching-tasks')">教学任务</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>系统概览</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="系统状态">
              <el-tag type="success">正常运行</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="AI服务">DeepSeek</el-descriptions-item>
            <el-descriptions-item label="不及格人数">
              <el-tag :type="overview.failCount > 0 ? 'danger' : 'success'">{{ overview.failCount ?? '--' }} 人</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="全校平均分">
              {{ overview.schoolAvgScore?.toFixed(1) ?? '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="全校及格率">
              {{ overview.schoolPassRate != null ? (overview.schoolPassRate * 100).toFixed(1) + '%' : '--' }}
            </el-descriptions-item>
            <el-descriptions-item label="版本">v1.0.0</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getSchoolOverview } from '@/api/stats'
import type { SchoolOverview } from '@/types'

const loading = ref(false)
const overview = reactive<Partial<SchoolOverview>>({})

const statCards = reactive([
  { title: '班级总数', value: '--' as string | number, icon: 'Collection', color: '#409EFF' },
  { title: '教师总数', value: '--' as string | number, icon: 'User', color: '#67C23A' },
  { title: '学生总数', value: '--' as string | number, icon: 'UserFilled', color: '#E6A23C' },
  { title: '课程总数', value: '--' as string | number, icon: 'Reading', color: '#F56C6C' }
])

onMounted(async () => {
  loading.value = true
  try {
    const data = await getSchoolOverview()
    if (data) {
      Object.assign(overview, data)
      statCards[0].value = data.totalClasses ?? '--'
      statCards[1].value = data.totalTeachers ?? '--'
      statCards[2].value = data.totalStudents ?? '--'
      statCards[3].value = data.totalCourses ?? '--'
    }
  } catch {
    // interceptor handles error
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stat-card { cursor: pointer; }
.stat-content { display: flex; justify-content: space-between; align-items: center; }
.stat-title { font-size: 14px; color: #909399; }
.stat-value { font-size: 28px; font-weight: 600; margin-top: 8px; }
.quick-actions { display: flex; flex-wrap: wrap; gap: 12px; }
</style>
