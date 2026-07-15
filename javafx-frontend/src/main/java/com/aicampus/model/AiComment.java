package com.aicampus.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiComment {
    @JsonProperty("id")
    private int id;
    @JsonProperty("semester")
    private String semester;
    @JsonProperty("content")
    private String content;
    @JsonProperty("isTeacherEdited")
    private boolean isTeacherEdited;
    @JsonProperty("createdAt")
    private String createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public boolean isTeacherEdited() { return isTeacherEdited; }
    public void setTeacherEdited(boolean teacherEdited) { isTeacherEdited = teacherEdited; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
