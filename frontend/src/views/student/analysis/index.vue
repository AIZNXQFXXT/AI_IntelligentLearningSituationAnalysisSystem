<template>
  <div>
    <el-card style="margin-bottom:20px">
      <template #header>成绩趋势</template>
      <div ref="trendRef" style="height:350px"></div>
    </el-card>
    <el-card>
      <template #header>能力雷达图</template>
      <div ref="radarRef" style="height:350px"></div>
    </el-card>
  </div>
</template>
<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { getStudentTrend, getStudentRadar } from '@/api/stats'
import { useUserStore } from '@/stores/user'
import type { TrendData, StudentRadar } from '@/types'
import * as echarts from 'echarts'

const userStore = useUserStore()
const trendRef = ref<HTMLElement>()
const radarRef = ref<HTMLElement>()
let trendChart: echarts.ECharts | null = null
let radarChart: echarts.ECharts | null = null

onMounted(async () => {
  const userId = userStore.userId
  const [trend, radar] = await Promise.all([
    getStudentTrend(userId),
    getStudentRadar(userId)
  ])
  await new Promise(r => setTimeout(r, 100))
  if (trendRef.value) {
    trendChart = echarts.init(trendRef.value)
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: trend.map(t => t.semester) },
      yAxis: { type: 'value', name: '平均分' },
      series: [{ data: trend.map(t => t.avgScore), type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, itemStyle: { color: '#409EFF' } }]
    })
  }
  if (radarRef.value && radar.length) {
    radarChart = echarts.init(radarRef.value)
    radarChart.setOption({
      radar: { indicator: radar.map(r => ({ name: r.courseName, max: 100 })) },
      series: [{ type: 'radar', data: [{ value: radar.map(r => r.score), name: '成绩' }] }]
    })
  }
})

onUnmounted(() => { trendChart?.dispose(); radarChart?.dispose() })
</script>
