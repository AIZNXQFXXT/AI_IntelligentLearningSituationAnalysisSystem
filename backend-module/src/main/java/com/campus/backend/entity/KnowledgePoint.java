package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("knowledge_point")
public class KnowledgePoint extends BaseEntity {
    private Long parentId;
    private String name;
    private String subjectType;
    private Integer level;
    private Integer sortOrder;
}