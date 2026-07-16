package com.campus.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.backend.entity.AICallLog;
import com.campus.backend.entity.OperationLog;
import com.campus.backend.mapper.AICallLogMapper;
import com.campus.backend.mapper.OperationLogMapper;
import com.campus.backend.service.LogService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@AllArgsConstructor
public class LogServiceImpl implements LogService {

    private final OperationLogMapper operationLogMapper;
    private final AICallLogMapper aiCallLogMapper;

    @Override
    public IPage<OperationLog> pageList(int page, int size,
                                        String username, String operation,
                                        String targetType, String resultStatus,
                                        LocalDate startDate, LocalDate endDate) {
        Page<OperationLog> p = new Page<>(page, size);
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (username != null && !username.isEmpty()) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (operation != null && !operation.isEmpty()) {
            wrapper.eq(OperationLog::getOperation, operation);
        }
        if (targetType != null && !targetType.isEmpty()) {
            wrapper.eq(OperationLog::getTargetType, targetType);
        }
        if (resultStatus != null && !resultStatus.isEmpty()) {
            wrapper.eq(OperationLog::getResultStatus, resultStatus);
        }
        if (startDate != null) {
            wrapper.ge(OperationLog::getCreatedAt, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(OperationLog::getCreatedAt, endDate.atTime(LocalTime.MAX));
        }

        wrapper.orderByDesc(OperationLog::getId);
        return operationLogMapper.selectPage(p, wrapper);
    }

    @Override
    public IPage<AICallLog> pageAiCalls(int page, int size) {
        Page<AICallLog> p = new Page<>(page, size);
        LambdaQueryWrapper<AICallLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AICallLog::getId);
        return aiCallLogMapper.selectPage(p, wrapper);
    }
}
