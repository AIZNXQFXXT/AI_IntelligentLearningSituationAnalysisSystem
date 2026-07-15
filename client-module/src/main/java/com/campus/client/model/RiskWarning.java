package com.campus.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RiskWarning {

    private Long id;
    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private String warningType;
    private String level;
    private String content;
    private Integer handled;
    private String handleResult;
    private String createTime;
    private String handleTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }
    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getWarningType() { return warningType; }
    public void setWarningType(String warningType) { this.warningType = warningType; }
    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Integer getHandled() { return handled; }
    public void setHandled(Integer handled) { this.handled = handled; }
    public String getHandleResult() { return handleResult; }
    public void setHandleResult(String handleResult) { this.handleResult = handleResult; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getHandleTime() { return handleTime; }
    public void setHandleTime(String handleTime) { this.handleTime = handleTime; }
}
