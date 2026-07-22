package com.aicampus.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Student {
    @JsonProperty("id")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int id;
    @JsonProperty("userId")
    private int userId;
    @JsonProperty("studentNo")
    private String studentNo;
    @JsonProperty("name")
    private String name;
    @JsonProperty("gender")
    private String gender;
    @JsonProperty("classId")
    private int classId;
    @JsonProperty("className")
    private String className;
    @JsonProperty("enrollYear")
    private int enrollYear;
    @JsonProperty("status")
    private int status;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("guardianPhone")
    private String guardianPhone;
    @JsonProperty("username")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String username;
    @JsonProperty("password")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;
    @JsonProperty("createdAt")
    private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public int getEnrollYear() { return enrollYear; }
    public void setEnrollYear(int enrollYear) { this.enrollYear = enrollYear; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getGuardianPhone() { return guardianPhone; }
    public void setGuardianPhone(String guardianPhone) { this.guardianPhone = guardianPhone; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
