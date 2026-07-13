export interface ClassStats {
  classId: number
  className: string
  courseId: number
  courseName: string
  avgScore: number
  minScore: number
  maxScore: number
  passRate: number
  excellentRate: number
  studentCount: number
}

export interface ScoreDistribution {
  range: string
  count: number
}

export interface TrendData {
  semester: string
  avgScore: number
}

export interface SchoolOverview {
  totalClasses: number
  totalTeachers: number
  totalStudents: number
  totalCourses: number
  schoolAvgScore: number
  schoolPassRate: number
  failCount: number
}

export interface StudentRadar {
  courseName: string
  score: number
}
