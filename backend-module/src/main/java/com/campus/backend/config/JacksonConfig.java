package com.campus.backend.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

/**
 * 全局 Jackson 时间格式配置：所有 LocalDateTime / LocalDate 统一序列化为 yyyy-MM-dd。
 * 客户端表格列直接显示 JSON 字符串，改此配置即全局生效，无需改客户端。
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            JavaTimeModule module = new JavaTimeModule();
            module.addSerializer(new LocalDateTimeSerializer(DATE_FORMATTER));
            module.addSerializer(new LocalDateSerializer(DATE_FORMATTER));
            builder.modulesToInstall(module);
        };
    }
}
