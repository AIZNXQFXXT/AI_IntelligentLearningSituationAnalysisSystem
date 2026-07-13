<template>
  <div v-loading="loading">
    <h2>教师控制台</h2>
    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="8" v-for="item in statCards" :key="item.title">
        <el-card shadow="hover">
          <div style="display:flex;justify-content:space-between;align-items:center">
            <div>
              <div style="font-size:14px;color:#909399">{{ item.title }}</div>
              <div style="font-size:28px;font-weight:600;margin-top:8px">{{ item.value }}</div>
            </div>
            <el-icon :size="40" :color="item.color"><component :is="item.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>快捷操作</template>
          <div style="display:flex;flex-direction:column;gap:12px">
            <el-button type="primary" @click="$router.push('/teacher/score/entry')">成绩录入</el-button>
            <el-button type="success" @click="$router.push('/teacher/score/import')">批量导入</el-button>
            <el-button type="warning" @click="$router.push('/teacher/diagnosis')">AI诊断</el-button>
            <el-button type="info" @click="$router.push('/teacher/comment')">评语管理</el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card>
          <template #header>功能说明</template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="本班学生">查看所带班级学生花名册和360度画像</el-descriptions-item>
            <el-descriptions-item label="成绩录入">单条录入或Excel批量导入学生成绩</el-descriptions-item>
            <el-descriptions-item label="班级统计">查看班级平均分、排名、分数分布等</el-descriptions-item>
            <el-descriptions-item label="AI诊断">生成学情诊断报告和个性化评语</el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getStudentPage } from '@/api/student'
import { getRiskWarningPage } from '@/api/riskWarning'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const loading = ref(false)

const statCards = reactive([
  { title: '所带学生数', value: '--' as string | number, icon: 'UserFilled', color: '#409EFF' },
  { title: '待处理预警', value: '--' as string | number, icon: 'WarningFilled', color: '#E6A23C' },
  { title: '用户名', value: userStore.username || '--', icon: 'User', color: '#67C23A' }
])

onMounted(async () => {
  loading.value = true
  try {
    const [studentRes, riskRes] = await Promise.all([
      getStudentPage({ page: 1, size: 1 }),
      getRiskWarningPage({ page: 1, size: 1, handleStatus: 'UNHANDLED' })
    ])
    statCards[0].value = studentRes.total ?? 0
    statCards[1].value = riskRes.total ?? 0
  } catch {
    // interceptor handles error
  } finally {
    loading.value = false
  }
})
</script>
