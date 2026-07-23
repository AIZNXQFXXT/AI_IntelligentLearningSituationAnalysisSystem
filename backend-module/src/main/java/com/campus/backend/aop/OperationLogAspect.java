package com.campus.backend.aop;

import com.campus.backend.entity.OperationLog;
import com.campus.backend.entity.User;
import com.campus.backend.mapper.OperationLogMapper;
import com.campus.backend.mapper.UserMapper;
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
    private static final String GUEST = "游客";

    private final OperationLogMapper logMapper;
    private final UserMapper userMapper;

    public OperationLogAspect(OperationLogMapper logMapper, UserMapper userMapper) {
        this.logMapper = logMapper;
        this.userMapper = userMapper;
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
                User user = userMapper.selectById(userId);
                String username = (user != null) ? user.getUsername() : null;
                log.setUsername((username != null && !username.isEmpty()) ? username : GUEST);
            } else {
                // 未登录或登录接口本身：标记为游客
                log.setUsername(GUEST);
            }

            logMapper.insert(log);
        }
    }
}