package com.campus.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "campus.ai")
public class AiProperties {
    private String provider = "deepseek";
    private String deepseekApiKey = "";
    private String deepseekBaseUrl = "https://api.deepseek.com";
    private String deepseekModel = "deepseek-chat";
    private String glmApiKey = "";
    private String glmBaseUrl = "https://open.bigmodel.cn/api/paas/v4";
    private String glmModel = "glm-4v-flash";
    private int dailyLimit = 200;
    private int timeoutConnect = 5000;
    private int timeoutRead = 30000;
    private int maxRetries = 2;
}