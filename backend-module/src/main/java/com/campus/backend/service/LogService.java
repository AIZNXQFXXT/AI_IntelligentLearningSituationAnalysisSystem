package com.campus.backend.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.backend.entity.AICallLog;
import com.campus.backend.entity.OperationLog;

import java.time.LocalDate;

public interface LogService {
    IPage<OperationLog> pageList(int page, int size,
                                 String username, String operation,
                                 String targetType, String resultStatus,
                                 LocalDate startDate, LocalDate endDate);

    IPage<AICallLog> pageAiCalls(int page, int size);
}
