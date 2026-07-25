package com.campus.backend.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LocalMockProvider implements AiService {

    @Override
    public AiResult call(AiRequest request) {
        long start = System.currentTimeMillis();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long duration = System.currentTimeMillis() - start;

        String mockContent = """
                {
                  "overall": "该生本学期表现良好，成绩稳定在班级中上水平。",
                  "strengths": [{"subject": "数学", "reason": "逻辑思维较强"}, {"subject": "物理", "reason": "概念理解深入"}],
                  "weaknesses": [{"subject": "英语", "reason": "词汇量不足"}, {"subject": "化学", "reason": "计算需加强"}],
                  "trend": "近三次考试成绩呈上升趋势",
                  "suggestions": ["建议每天背诵20个英语单词", "化学部分重点复习摩尔计算"],
                  "riskLevel": "LOW"
                }""";

        log.info("[MockProvider] 返回模拟诊断结果");
        return AiResult.success(mockContent, 100, 50, duration);
    }
}