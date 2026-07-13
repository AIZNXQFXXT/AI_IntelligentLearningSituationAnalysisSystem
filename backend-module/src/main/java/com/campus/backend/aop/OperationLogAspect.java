package com.campus.backend.aop;

import com.campus.backend.entity.OperationLog;
import com.campus.backend.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class OperationLogAspect {
    private final OperationLogMapper logMapper;

    public OperationLogAspect(OperationLogMapper logMapper) {
        this.logMapper = logMapper;
    }

    @Around("@annotation(org.springframework.web.bind.annotation.PostMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping) " +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String resultStatus = "SUCCESS";
        String failReason = null;

        try {
            Object result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            resultStatus = "FAILED";
            failReason = e.getMessage();
            throw e;
        } finally {
            long duration = System.currentTimeMillis() - start;
            HttpServletRequest request = ((ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes()).getRequest();

            OperationLog log = new OperationLog();
            log.setOperation(request.getMethod());
            log.setDetail(joinPoint.getSignature().toShortString());
            log.setDurationMs(duration);
            log.setResultStatus(resultStatus);
            log.setFailReason(failReason);
            log.setIp(request.getRemoteAddr());

            Long userId = (Long) request.getAttribute("userId");
            if (userId != null) {
                log.setOperatorId(userId);
            }

            logMapper.insert(log);
        }
    }
}