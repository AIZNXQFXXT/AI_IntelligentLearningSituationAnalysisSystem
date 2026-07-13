<template>
  <el-container class="layout-container">
    <el-aside :width="collapsed ? '64px' : '220px'" class="layout-aside">
      <div class="logo">
        <img src="" alt="" style="width:32px;height:32px" />
        <span v-if="!collapsed" class="logo-title">AI学情分析</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        router
        background-color="#001529"
        text-color="#ffffffa6"
        active-text-color="#ffffff"
        class="layout-menu"
      >
        <template v-for="item in menuItems" :key="item.path">
          <el-menu-item :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="appStore.toggleSidebar">
            <Fold v-if="!collapsed" />
            <Expand v-else />
          </el-icon>
          <span class="page-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ userStore.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const collapsed = computed(() => appStore.sidebarCollapsed)
const activeMenu = computed(() => route.path)
const currentTitle = computed(() => (route.meta.title as string) || '')

interface MenuItem {
  path: string
  title: string
  icon: string
}

const adminMenus: MenuItem[] = [
  { path: '/admin/dashboard', title: '控制台', icon: 'DataAnalysis' },
  { path: '/admin/classes', title: '班级管理', icon: 'Collection' },
  { path: '/admin/teachers', title: '教师管理', icon: 'User' },
  { path: '/admin/students', title: '学生管理', icon: 'UserFilled' },
  { path: '/admin/courses', title: '课程管理', icon: 'Reading' },
  { path: '/admin/exams', title: '考试批次', icon: 'EditPen' },
  { path: '/admin/teaching-tasks', title: '教学任务', icon: 'List' },
  { path: '/admin/stats', title: '全校学情', icon: 'TrendCharts' },
  { path: '/admin/config', title: '系统配置', icon: 'Setting' },
  { path: '/admin/logs', title: '操作日志', icon: 'Document' },
  { path: '/admin/ai-logs', title: 'AI调用日志', icon: 'Monitor' }
]

const teacherMenus: MenuItem[] = [
  { path: '/teacher/dashboard', title: '控制台', icon: 'DataAnalysis' },
  { path: '/teacher/students', title: '本班学生', icon: 'UserFilled' },
  { path: '/teacher/score/entry', title: '成绩录入', icon: 'Edit' },
  { path: '/teacher/score/import', title: '批量导入', icon: 'Upload' },
  { path: '/teacher/stats', title: '班级统计', icon: 'TrendCharts' },
  { path: '/teacher/diagnosis', title: 'AI诊断', icon: 'MagicStick' },
  { path: '/teacher/comment', title: '评语管理', icon: 'ChatLineSquare' },
  { path: '/teacher/risk', title: '风险预警', icon: 'WarningFilled' },
  { path: '/teacher/report', title: '报表导出', icon: 'Download' }
]

const studentMenus: MenuItem[] = [
  { path: '/student/dashboard', title: '控制台', icon: 'DataAnalysis' },
  { path: '/student/scores', title: '成绩查询', icon: 'Document' },
  { path: '/student/analysis', title: '成绩分析', icon: 'TrendCharts' },
  { path: '/student/diagnosis', title: '诊断报告', icon: 'MagicStick' },
  { path: '/student/advice', title: '学习建议', icon: 'Promotion' },
  { path: '/student/comment', title: '期末评语', icon: 'ChatLineSquare' },
  { path: '/student/risk', title: '我的预警', icon: 'WarningFilled' }
]

const menuItems = computed<MenuItem[]>(() => {
  const role = userStore.role
  if (role === 'ADMIN') return adminMenus
  if (role === 'TEACHER') return teacherMenus
  return studentMenus
})

function handleLogout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background: #001529;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid #ffffff1a;
}

.logo-title {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

.layout-menu {
  border-right: none;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e8e8e8;
  background: #fff;
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
}

.page-title {
  font-size: 16px;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
}

.layout-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
