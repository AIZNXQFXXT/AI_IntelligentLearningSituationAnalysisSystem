package com.campus.backend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_record")
public class TaskRecord extends BaseEntity {
    private String taskType;       // SCORE_IMPORT / AI_COMMENT_BATCH / EXPORT
    private String status;         // PENDING → PROCESSING → COMPLETED → FAILED
    private Integer progress;      // 0-100
    private Integer currentCount;
    private Integer totalCount;
    private String fileUrl;
    private String resultJson;     // 导入结果：{success:10, failed:2, errors:[{row:5, reason:"..."}]}
    private String errorMessage;
}