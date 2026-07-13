<template>
  <div v-loading="loading">
    <h2>学生控制台</h2>
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

    <el-row :gutter="20" style="margin-bottom:20px">
      <el-col :span="12">
        <el-card>
          <template #header>最近成绩</template>
          <el-table :data="recentScores" size="small" stripe>
            <el-table-column prop="courseId" label="课程ID" width="80" />
            <el-table-column prop="finalScore" label="成绩" width="80">
              <template #default="{ row }">
                <span style="font-weight:600">{{ row.finalScore }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="rankClass" label="班级排名" width="90" />
            <el-table-column prop="rankGrade" label="年级排名" width="90" />
          </el-table>
          <el-empty v-if="recentScores.length === 0" description="暂无成绩数据" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>我的预警</template>
          <el-table :data="recentWarnings" size="small" stripe>
            <el-table-column prop="semester" label="学期" width="100" />
            <el-table-column prop="riskLevel" label="等级" width="80">
              <template #default="{ row }">
                <el-tag :type="row.riskLevel === 'HIGH' ? 'danger' : row.riskLevel === 'MEDIUM' ? 'warning' : 'success'" size="small">
                  {{ row.riskLevel }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="riskReason" label="原因" show-overflow-tooltip />
            <el-table-column prop="handleStatus" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="row.handleStatus === 'HANDLED' ? 'success' : 'danger'" size="small">
                  {{ row.handleStatus === 'HANDLED' ? '已处理' : '未处理' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="recentWarnings.length === 0" description="暂无预警信息" :image-size="60" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="8" v-for="item in cards" :key="item.title">
        <el-card shadow="hover" style="cursor:pointer" @click="$router.push(item.path)">
          <div style="text-align:center">
            <el-icon :size="40" :color="item.color"><component :is="item.icon" /></el-icon>
            <div style="font-size:16px;font-weight:500;margin-top:12px">{{ item.title }}</div>
            <div style="color:#909399;font-size:13px;margin-top:4px">{{ item.desc }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getScorePage } from '@/api/score'
import { getMyWarnings } from '@/api/riskWarning'
import { useUserStore } from '@/stores/user'
import type { Score, RiskWarning } from '@/types'

const userStore = useUserStore()
const loading = ref(false)
const recentScores = ref<Score[]>([])
const recentWarnings = ref<RiskWarning[]>([])

const statCards = reactive([
  { title: '最新平均分', value: '--' as string | number, icon: 'TrendCharts', color: '#409EFF' },
  { title: '预警数量', value: '--' as string | number, icon: 'WarningFilled', color: '#E6A23C' },
  { title: '欢迎', value: userStore.username || '--', icon: 'User', color: '#67C23A' }
])

const cards = [
  { title: '成绩查询', desc: '查看各科成绩和班级排名', icon: 'Document', color: '#409EFF', path: '/student/scores' },
  { title: '成绩分析', desc: '查看成绩趋势和能力雷达图', icon: 'TrendCharts', color: '#67C23A', path: '/student/analysis' },
  { title: '诊断报告', desc: '查看AI学情诊断分析', icon: 'MagicStick', color: '#E6A23C', path: '/student/diagnosis' },
  { title: '学习建议', desc: '查看个性化学习提升方案', icon: 'Promotion', color: '#F56C6C', path: '/student/advice' },
  { title: '期末评语', desc: '查看教师评语', icon: 'ChatLineSquare', color: '#909399', path: '/student/comment' },
  { title: '我的预警', desc: '查看学业预警信息', icon: 'WarningFilled', color: '#E6A23C', path: '/student/risk' }
]

onMounted(async () => {
  loading.value = true
  try {
    const [scoreRes, warningRes] = await Promise.all([
      getScorePage({ page: 1, size: 5 }),
      getMyWarnings()
    ])
    recentScores.value = scoreRes.records.slice(0, 5)
    recentWarnings.value = (warningRes || []).slice(0, 5)

    if (scoreRes.records.length > 0) {
      const avg = scoreRes.records.reduce((sum, s) => sum + (s.finalScore || 0), 0) / scoreRes.records.length
      statCards[0].value = avg.toFixed(1)
    }
    statCards[1].value = (warningRes || []).length
  } catch {
    // interceptor handles error
  } finally {
    loading.value = false
  }
})
</script>
