package com.campus.common.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SuggestionVO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String className;
    private String semester;
    private List<String> shortTerm;
    private List<String> longTerm;
    private String dailyPlan;
    private List<ResourceItem> resources;
    private LocalDateTime createdAt;

    @Data
    public static class ResourceItem {
        private String subject;
        private List<String> items;
    }
}
