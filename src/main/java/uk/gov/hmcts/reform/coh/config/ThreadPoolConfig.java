package uk.gov.hmcts.reform.coh.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ThreadPoolConfig {

    private static int errorCount;

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolConfig.class);

    public static int getUnhandledTaskExceptionCount() {
        return errorCount;
    }

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();

        threadPoolTaskScheduler.setThreadNamePrefix("EventNotifierTask-");
        threadPoolTaskScheduler.setErrorHandler(t -> {
            log.error("Unhandled exception during task. {}: {}", t.getClass(), t.getMessage(), t);
            errorCount++;
        });

        return threadPoolTaskScheduler;
    }
}