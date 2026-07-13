<template>
  <div>
    <el-card>
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>班级统计</span>
          <div style="display:flex;gap:12px">
            <el-select v-model="classId" placeholder="选择班级" style="width:180px" @change="loadStats">
              <el-option v-for="c in classes" :key="c.id" :label="c.grade + ' ' + c.className" :value="c.id" />
            </el-select>
            <el-select v-model="courseId" placeholder="选择课程" style="width:160px" @change="loadStats">
              <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </div>
        </div>
      </template>
      <el-row :gutter="20" v-if="stats">
        <el-col :span="4" v-for="item in statItems" :key="item.label">
          <el-card shadow="never">
            <div style="text-align:center">
              <div style="color:#909399;font-size:13px">{{ item.label }}</div>
              <div style="font-size:24px;font-weight:600;margin-top:6px">{{ item.value }}</div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </el-card>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card><template #header>分数分布</template><div ref="distRef" style="height:350px"></div></el-card>
      </el-col>
      <el-col :span="12">
        <el-card><template #header>学期趋势</template><div ref="trendRef" style="height:350px"></div></el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { getClassStats, getScoreDistribution, getTrend } from '@/api/stats'
import { getAllClasses } from '@/api/classInfo'
import { getAllCourses } from '@/api/course'
import type { ClassInfo, Course, ClassStats, ScoreDistribution, TrendData } from '@/types'
import * as echarts from 'echarts'

const classes = ref<ClassInfo[]>([])
const courses = ref<Course[]>([])
const classId = ref<number>()
const courseId = ref<number>()
const stats = ref<ClassStats | null>(null)
const distribution = ref<ScoreDistribution[]>([])
const trend = ref<TrendData[]>([])
const distRef = ref<HTMLElement>()
const trendRef = ref<HTMLElement>()
let distChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

const statItems = computed(() => {
  if (!stats.value) return []
  return [
    { label: '平均分', value: stats.value.avgScore?.toFixed(1) },
    { label: '最高分', value: stats.value.maxScore?.toFixed(1) },
    { label: '最低分', value: stats.value.minScore?.toFixed(1) },
    { label: '及格率', value: (stats.value.passRate * 100).toFixed(1) + '%' },
    { label: '优秀率', value: (stats.value.excellentRate * 100).toFixed(1) + '%' },
    { label: '学生数', value: stats.value.studentCount }
  ]
})

async function loadOptions() {
  const [classRes, courseRes] = await Promise.all([getAllClasses(), getAllCourses()])
  classes.value = classRes
  courses.value = courseRes
}

async function loadStats() {
  if (!classId.value || !courseId.value) return
  const [s, d, t] = await Promise.all([
    getClassStats(classId.value, courseId.value),
    getScoreDistribution(classId.value, courseId.value),
    getTrend(classId.value)
  ])
  stats.value = s
  distribution.value = d
  trend.value = t
  await nextTick()
  renderCharts()
}

function renderCharts() {
  if (distRef.value) {
    distChart = distChart || echarts.init(distRef.value)
    distChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: distribution.value.map(d => d.range) },
      yAxis: { type: 'value', name: '人数' },
      series: [{ data: distribution.value.map(d => d.count), type: 'bar', itemStyle: { color: '#409EFF' } }]
    })
  }
  if (trendRef.value) {
    trendChart = trendChart || echarts.init(trendRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: trend.value.map(t => t.semester) },
      yAxis: { type: 'value', name: '平均分' },
      series: [{ data: trend.value.map(t => t.avgScore), type: 'line', smooth: true, itemStyle: { color: '#67C23A' } }]
    })
  }
}

onMounted(loadOptions)
onUnmounted(() => { distChart?.dispose(); trendChart?.dispose() })
</script>
