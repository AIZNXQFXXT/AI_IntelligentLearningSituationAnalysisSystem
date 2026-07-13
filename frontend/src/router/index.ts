import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { setupRouterGuard } from './guard'

const Layout = () => import('@/components/Layout/index.vue')

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/login',
    children: [
      // ===== Admin =====
      {
        path: 'admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard.vue'),
        meta: { title: '控制台', role: 'ADMIN' }
      },
      {
        path: 'admin/classes',
        name: 'AdminClasses',
        component: () => import('@/views/admin/class/index.vue'),
        meta: { title: '班级管理', role: 'ADMIN' }
      },
      {
        path: 'admin/teachers',
        name: 'AdminTeachers',
        component: () => import('@/views/admin/teacher/index.vue'),
        meta: { title: '教师管理', role: 'ADMIN' }
      },
      {
        path: 'admin/students',
        name: 'AdminStudents',
        component: () => import('@/views/admin/student/index.vue'),
        meta: { title: '学生管理', role: 'ADMIN' }
      },
      {
        path: 'admin/courses',
        name: 'AdminCourses',
        component: () => import('@/views/admin/course/index.vue'),
        meta: { title: '课程管理', role: 'ADMIN' }
      },
      {
        path: 'admin/exams',
        name: 'AdminExams',
        component: () => import('@/views/admin/exam/index.vue'),
        meta: { title: '考试批次', role: 'ADMIN' }
      },
      {
        path: 'admin/teaching-tasks',
        name: 'AdminTeachingTasks',
        component: () => import('@/views/admin/teachingTask/index.vue'),
        meta: { title: '教学任务', role: 'ADMIN' }
      },
      {
        path: 'admin/stats',
        name: 'AdminStats',
        component: () => import('@/views/admin/stats/index.vue'),
        meta: { title: '全校学情', role: 'ADMIN' }
      },
      {
        path: 'admin/config',
        name: 'AdminConfig',
        component: () => import('@/views/admin/config/index.vue'),
        meta: { title: '系统配置', role: 'ADMIN' }
      },
      {
        path: 'admin/logs',
        name: 'AdminLogs',
        component: () => import('@/views/admin/log/index.vue'),
        meta: { title: '操作日志', role: 'ADMIN' }
      },
      {
        path: 'admin/ai-logs',
        name: 'AdminAiLogs',
        component: () => import('@/views/admin/aiLog/index.vue'),
        meta: { title: 'AI调用日志', role: 'ADMIN' }
      },

      // ===== Teacher =====
      {
        path: 'teacher/dashboard',
        name: 'TeacherDashboard',
        component: () => import('@/views/teacher/dashboard.vue'),
        meta: { title: '控制台', role: 'TEACHER' }
      },
      {
        path: 'teacher/students',
        name: 'TeacherStudents',
        component: () => import('@/views/teacher/students/index.vue'),
        meta: { title: '本班学生', role: 'TEACHER' }
      },
      {
        path: 'teacher/score/entry',
        name: 'TeacherScoreEntry',
        component: () => import('@/views/teacher/score/entry.vue'),
        meta: { title: '成绩录入', role: 'TEACHER' }
      },
      {
        path: 'teacher/score/import',
        name: 'TeacherScoreImport',
        component: () => import('@/views/teacher/score/import.vue'),
        meta: { title: '批量导入', role: 'TEACHER' }
      },
      {
        path: 'teacher/stats',
        name: 'TeacherStats',
        component: () => import('@/views/teacher/stats/index.vue'),
        meta: { title: '班级统计', role: 'TEACHER' }
      },
      {
        path: 'teacher/diagnosis',
        name: 'TeacherDiagnosis',
        component: () => import('@/views/teacher/diagnosis/index.vue'),
        meta: { title: 'AI诊断', role: 'TEACHER' }
      },
      {
        path: 'teacher/comment',
        name: 'TeacherComment',
        component: () => import('@/views/teacher/comment/index.vue'),
        meta: { title: '评语管理', role: 'TEACHER' }
      },
      {
        path: 'teacher/risk',
        name: 'TeacherRisk',
        component: () => import('@/views/teacher/risk/index.vue'),
        meta: { title: '风险预警', role: 'TEACHER' }
      },
      {
        path: 'teacher/report',
        name: 'TeacherReport',
        component: () => import('@/views/teacher/report/index.vue'),
        meta: { title: '报表导出', role: 'TEACHER' }
      },

      // ===== Student =====
      {
        path: 'student/dashboard',
        name: 'StudentDashboard',
        component: () => import('@/views/student/dashboard.vue'),
        meta: { title: '控制台', role: 'STUDENT' }
      },
      {
        path: 'student/scores',
        name: 'StudentScores',
        component: () => import('@/views/student/scores/index.vue'),
        meta: { title: '成绩查询', role: 'STUDENT' }
      },
      {
        path: 'student/analysis',
        name: 'StudentAnalysis',
        component: () => import('@/views/student/analysis/index.vue'),
        meta: { title: '成绩分析', role: 'STUDENT' }
      },
      {
        path: 'student/diagnosis',
        name: 'StudentDiagnosis',
        component: () => import('@/views/student/diagnosis/index.vue'),
        meta: { title: '诊断报告', role: 'STUDENT' }
      },
      {
        path: 'student/advice',
        name: 'StudentAdvice',
        component: () => import('@/views/student/advice/index.vue'),
        meta: { title: '学习建议', role: 'STUDENT' }
      },
      {
        path: 'student/comment',
        name: 'StudentComment',
        component: () => import('@/views/student/comment/index.vue'),
        meta: { title: '期末评语', role: 'STUDENT' }
      },
      {
        path: 'student/risk',
        name: 'StudentRisk',
        component: () => import('@/views/student/risk/index.vue'),
        meta: { title: '我的预警', role: 'STUDENT' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

setupRouterGuard(router)

export default router
