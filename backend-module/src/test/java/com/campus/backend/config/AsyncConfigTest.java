package com.campus.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import static org.junit.jupiter.api.Assertions.*;

class AsyncConfigTest {

    @Test
    void importExecutor_shouldHaveCorrectConfig() {
        AsyncConfig config = new AsyncConfig();
        ThreadPoolTaskExecutor executor = config.importExecutor();

        assertNotNull(executor);
        assertEquals(2, executor.getCorePoolSize());
        assertEquals(4, executor.getMaxPoolSize());
        assertTrue(executor.getThreadNamePrefix().startsWith("import-"));
        executor.shutdown();
    }
}
