package com.campus.backend.aop;

import com.campus.backend.ai.AiRequest;
import com.campus.backend.ai.AiResult;
import com.campus.backend.entity.AICallLog;
import com.campus.backend.mapper.AICallLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AiCallLogAspect {

    private final AICallLogMapper aiCallLogMapper;
    private final ObjectMapper objectMapper;

    @Around("execution(* com.campus.backend.ai.AiServiceFactory.execute(..))")
    public Object logAiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        AiRequest request = (AiRequest) joinPoint.getArgs()[0];
        long start = System.currentTimeMillis();

        try {
            AiResult result = (AiResult) joinPoint.proceed();
            saveLog(request, result, null, System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            saveLog(request, null, e.getMessage(), duration);
            throw e;
        }
    }

    private void saveLog(AiRequest req, AiResult result, String errorMsg, long durationMs) {
        try {
            AICallLog log = new AICallLog();
            log.setCallerId(req.getCallerId());
            log.setFunctionName(req.getFunctionName());
            log.setAiModel(req.getModel());
            log.setPromptTemplate(req.getPromptTemplate());
            log.setRequestBody(req.getPrompt().substring(0, Math.min(req.getPrompt().length(), 500)));
            log.setDurationMs((int) durationMs);

            if (result != null) {
                log.setSuccess(result.isSuccess() ? 1 : 0);
                log.setTokensInput(result.getTokensInput());
                log.setTokensOutput(result.getTokensOutput());
                log.setTokensTotal(result.getTokensTotal());
                log.setEstimatedCost(BigDecimal.valueOf(result.getEstimatedCost()));
                log.setResponseBody(result.getContent() != null ?
                        result.getContent().substring(0, Math.min(result.getContent().length(), 500)) : null);
                log.setErrorMessage(result.getErrorMessage());
            } else {
                log.setSuccess(0);
                log.setErrorMessage(errorMsg);
            }
            aiCallLogMapper.insert(log);
        } catch (Exception e) {
            log.error("Failed to save AI call log", e);
        }
    }
}