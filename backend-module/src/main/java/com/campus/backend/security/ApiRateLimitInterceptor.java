package com.campus.backend.security;

import com.campus.common.enums.ErrorCode;
import com.campus.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.List;

@Component
public class ApiRateLimitInterceptor implements HandlerInterceptor {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final StringRedisTemplate redisTemplate;
    private final List<RateLimitRule> rules;

    public ApiRateLimitInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.rules = buildRules();
    }

    private static List<RateLimitRule> buildRules() {
        return List.of(
                new RateLimitRule("/api/auth/login", "POST", KeyType.IP, 5, 60, "login"),
                new RateLimitRule("/api/tasks", "POST", KeyType.USER, 10, 60, "tasks"),
                new RateLimitRule("/api/diagnoses", "POST", KeyType.USER, 20, 86400, "diagnoses"),
                new RateLimitRule("/api/reports/excel/**", "GET", KeyType.USER, 5, 60, "reports")
        );
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        for (RateLimitRule rule : rules) {
            if (!rule.matches(request)) {
                continue;
            }
            String identifier = resolveIdentifier(rule, request);
            String windowSlot = resolveWindowSlot(rule.windowSeconds);
            String redisKey = "ratelimit:" + rule.name + ":" + identifier + ":" + windowSlot;

            Long count = redisTemplate.opsForValue().increment(redisKey);
            if (count == null) {
                return true;
            }
            if (count == 1) {
                redisTemplate.expire(redisKey, Duration.ofSeconds(rule.windowSeconds));
            }
            if (count > rule.limit) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
            }
            break;
        }
        return true;
    }

    private String resolveIdentifier(RateLimitRule rule, HttpServletRequest request) {
        return switch (rule.keyType) {
            case IP -> getClientIp(request);
            case USER -> {
                Long userId = (Long) request.getAttribute("userId");
                if (userId == null) {
                    throw new BusinessException(ErrorCode.UNAUTHORIZED);
                }
                yield String.valueOf(userId);
            }
        };
    }

    private static String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static String resolveWindowSlot(int windowSeconds) {
        long epochSecond = System.currentTimeMillis() / 1000;
        long slot = epochSecond / windowSeconds;
        return String.valueOf(slot);
    }

    @Getter
    @AllArgsConstructor
    private static class RateLimitRule {
        private final String pathPattern;
        private final String httpMethod;
        private final KeyType keyType;
        private final int limit;
        private final int windowSeconds;
        private final String name;

        public boolean matches(HttpServletRequest request) {
            if (httpMethod != null && !request.getMethod().equalsIgnoreCase(httpMethod)) {
                return false;
            }
            return PATH_MATCHER.match(pathPattern, request.getRequestURI());
        }
    }

    private enum KeyType {
        IP, USER
    }
}
