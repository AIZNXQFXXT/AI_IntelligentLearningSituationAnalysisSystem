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

        String mockContent = "{\n" +
                "  \"overall\": \"该生本学期表现良好，成绩稳定在班级中上水平。\",\n" +
                "  \"strengths\": [\"数学逻辑思维较强\", \"物理概念理解深入\"],\n" +
                "  \"weaknesses\": [\"英语词汇量不足\", \"化学计算需加强\"],\n" +
                "  \"trend\": \"近三次考试成绩呈上升趋势\",\n" +
                "  \"suggestions\": [\"建议每天背诵20个英语单词\", \"化学部分重点复习摩尔计算\"],\n" +
                "  \"riskLevel\": \"LOW\"\n" +
                "}";

        log.info("[MockProvider] 返回模拟诊断结果");
        return AiResult.success(mockContent, 100, 50, duration);
    }
}