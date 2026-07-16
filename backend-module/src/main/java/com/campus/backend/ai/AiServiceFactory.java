package com.campus.backend.ai;

import com.campus.backend.config.AiProperties;
import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceFactory {

    private final List<AiService> providers;
    private final AiRateLimiter rateLimiter;
    private final AiProperties aiProperties;
    private final Map<String, AiService> providerMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (AiService provider : providers) {
            String name = provider.getClass().getSimpleName()
                    .replace("Provider", "")
                    .toLowerCase();
            providerMap.put(name, provider);
        }
        log.info("AI providers registered: {}", providerMap.keySet());
    }

    public AiResult execute(AiRequest request) {
        if (!rateLimiter.allowRequest(request.getCallerId())) {
            throw new BusinessException(429, "AI 每日调用次数已达上限（" + aiProperties.getDailyLimit() + "次），请明天再试");
        }
        return executeWithRetry(request);
    }

    private AiResult executeWithRetry(AiRequest request) {
        AiResult lastResult = null;
        int maxRetries = aiProperties.getMaxRetries();

        for (int i = 0; i <= maxRetries; i++) {
            try {
                AiService primary = providerMap.get("deepseek");
                if (primary != null) {
                    AiResult result = primary.call(request);
                    if (result.isSuccess()) {
                        return result;
                    }
                    lastResult = result;
                    log.warn("DeepSeek attempt {} failed: {}", i + 1, result.getErrorMessage());
                }
            } catch (Exception e) {
                lastResult = AiResult.error(e.getMessage(), 0);
                log.warn("DeepSeek attempt {} exception: {}", i + 1, e.getMessage());
            }

            if (i < maxRetries) {
                try {
                    Thread.sleep(1000L * (i + 1));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        AiService mock = providerMap.get("localmock");
        if (mock != null) {
            log.warn("Falling back to LocalMockProvider");
            return mock.call(request);
        }

        String errorMsg = lastResult != null ? lastResult.getErrorMessage() : "AI 服务调用失败，请稍后重试";
        throw new BusinessException(ErrorCode.AI_SERVICE_ERROR.getCode(), "AI 服务异常: " + errorMsg);
    }
}