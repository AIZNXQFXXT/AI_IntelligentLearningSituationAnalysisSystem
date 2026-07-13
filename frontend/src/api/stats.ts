import request from '@/utils/request'
import type { ApiResponse, ClassStats, ScoreDistribution, TrendData, SchoolOverview, StudentRadar } from '@/types'

export function getClassStats(classId: number, courseId: number) {
  return request.get<ApiResponse<ClassStats>>(`/stats/class/${classId}/course/${courseId}`).then(res => res.data.data)
}

export function getScoreDistribution(classId: number, courseId: number) {
  return request.get<ApiResponse<ScoreDistribution[]>>(`/stats/class/${classId}/course/${courseId}/distribution`).then(res => res.data.data)
}

export function getTrend(classId: number) {
  return request.get<ApiResponse<TrendData[]>>(`/stats/class/${classId}/trend`).then(res => res.data.data)
}

export function getSchoolOverview() {
  return request.get<ApiResponse<SchoolOverview>>('/stats/overview').then(res => res.data.data)
}

export function getStudentRadar(studentId: number) {
  return request.get<ApiResponse<StudentRadar[]>>(`/stats/student/${studentId}/radar`).then(res => res.data.data)
}

export function getStudentTrend(studentId: number) {
  return request.get<ApiResponse<TrendData[]>>(`/stats/student/${studentId}/trend`).then(res => res.data.data)
}
