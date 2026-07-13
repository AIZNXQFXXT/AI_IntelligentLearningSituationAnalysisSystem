<template>
  <div v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in overviewCards" :key="item.label">
        <el-card shadow="hover">
          <div style="text-align:center">
            <div style="font-size:14px;color:#909399">{{ item.label }}</div>
            <div style="font-size:28px;font-weight:600;margin-top:8px">{{ item.value }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top:20px">
      <template #header>
        <div style="display:flex;justify-content:space-between;align-items:center">
          <span>全校学情概览</span>
          <div style="display:flex;gap:12px">
            <el-select v-model="selectedClassId" placeholder="选择班级" clearable style="width:180px" @change="loadDistribution">
              <el-option v-for="c in classes" :key="c.id" :label="c.grade + ' ' + c.className" :value="c.id" />
            </el-select>
            <el-select v-model="selectedCourseId" placeholder="选择课程" clearable style="width:160px" @change="loadDistribution">
              <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </div>
        </div>
      </template>
      <div ref="chartRef" style="height:400px;width:100%"></div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { getSchoolOverview, getScoreDistribution } from '@/api/stats'
import { getAllClasses } from '@/api/classInfo'
import { getAllCourses } from '@/api/course'
import type { SchoolOverview, ScoreDistribution, ClassInfo, Course } from '@/types'
import * as echarts from 'echarts'

const loading = ref(false)
const chartRef = ref<HTMLElement>()
let chart: echarts.ECharts | null = null

const overview = reactive<Partial<SchoolOverview>>({})
const classes = ref<ClassInfo[]>([])
const courses = ref<Course[]>([])
const selectedClassId = ref<number>()
const selectedCourseId = ref<number>()
const distribution = ref<ScoreDistribution[]>([])

const overviewCards = reactive([
  { label: '全校平均分', value: '--' as string | number },
  { label: '及格率', value: '--' as string | number },
  { label: '不及格人数', value: '--' as string | number },
  { label: '学生总数', value: '--' as string | number }
])

async function loadOverview() {
  const data = await getSchoolOverview()
  if (data) {
    Object.assign(overview, data)
    overviewCards[0].value = data.schoolAvgScore?.toFixed(1) ?? '--'
    overviewCards[1].value = data.schoolPassRate != null ? (data.schoolPassRate * 100).toFixed(1) + '%' : '--'
    overviewCards[2].value = data.failCount ?? '--'
    overviewCards[3].value = data.totalStudents ?? '--'
  }
}

async function loadDistribution() {
  if (!selectedClassId.value || !selectedCourseId.value) return
  const data = await getScoreDistribution(selectedClassId.value, selectedCourseId.value)
  distribution.value = data || []
  await nextTick()
  renderChart()
}

function renderChart() {
  if (!chartRef.value) return
  if (!chart) {
    chart = echarts.init(chartRef.value)
  }
  chart.setOption({
    title: { text: '成绩分布', left: 'center' },
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: distribution.value.map(d => d.range),
      axisLabel: { rotate: 30 }
    },
    yAxis: { type: 'value', name: '人数' },
    series: [{
      data: distribution.value.map(d => d.count),
      type: 'bar',
      itemStyle: {
        color: (params: any) => {
          const colors = ['#67C23A', '#67C23A', '#409EFF', '#409EFF', '#E6A23C', '#E6A23C', '#F56C6C', '#F56C6C', '#F56C6C', '#F56C6C']
          return colors[params.dataIndex] || '#409EFF'
        }
      }
    }],
    grid: { left: '3%', right: '4%', bottom: '15%', containLabel: true }
  })
}

function handleResize() {
  chart?.resize()
}

onMounted(async () => {
  loading.value = true
  try {
    const [classRes, courseRes] = await Promise.all([getAllClasses(), getAllCourses()])
    classes.value = classRes
    courses.value = courseRes
    await loadOverview()
  } finally {
    loading.value = false
  }
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>
