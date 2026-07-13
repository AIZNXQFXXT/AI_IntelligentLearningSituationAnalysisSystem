import request from '@/utils/request'
import type { ApiResponse, PageResult, Course, CourseForm } from '@/types'

export function getCoursePage(params: { page?: number; size?: number; keyword?: string; type?: string }) {
  return request.get<ApiResponse<PageResult<Course>>>('/courses', { params }).then(res => res.data.data)
}

export function getAllCourses() {
  return request.get<ApiResponse<Course[]>>('/courses/list').then(res => res.data.data)
}

export function createCourse(data: CourseForm) {
  return request.post<ApiResponse<void>>('/courses', data).then(res => res.data)
}

export function updateCourse(id: number, data: CourseForm) {
  return request.put<ApiResponse<void>>(`/courses/${id}`, data).then(res => res.data)
}

export function deleteCourse(id: number) {
  return request.delete<ApiResponse<void>>(`/courses/${id}`).then(res => res.data)
}
