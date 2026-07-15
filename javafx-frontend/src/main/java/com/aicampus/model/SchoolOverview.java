package com.aicampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SchoolOverview {
    @JsonProperty("classCount")
    private long totalClasses;
    @JsonProperty("teacherCount")
    private long totalTeachers;
    @JsonProperty("studentCount")
    private long totalStudents;
    @JsonProperty("courseCount")
    private long totalCourses;

    public long getTotalClasses() { return totalClasses; }
    public void setTotalClasses(long totalClasses) { this.totalClasses = totalClasses; }
    public long getTotalTeachers() { return totalTeachers; }
    public void setTotalTeachers(long totalTeachers) { this.totalTeachers = totalTeachers; }
    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }
    public long getTotalCourses() { return totalCourses; }
    public void setTotalCourses(long totalCourses) { this.totalCourses = totalCourses; }
}
