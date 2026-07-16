package com.campus.backend.ai;

import com.campus.backend.config.AiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AiRateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final AiProperties aiProperties;

    public boolean allowRequest(Long userId) {
        String dateStr = LocalDate.now().toString();
        String key = "ai:daily:" + userId + ":" + dateStr;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            return true;
        }
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofDays(1));
        }
        return count <= aiProperties.getDailyLimit();
    }

    public long getRemainingQuota(Long userId) {
        String dateStr = LocalDate.now().toString();
        String key = "ai:daily:" + userId + ":" + dateStr;
        String val = redisTemplate.opsForValue().get(key);
        long used = val == null ? 0 : Long.parseLong(val);
        return Math.max(0, aiProperties.getDailyLimit() - used);
    }
}