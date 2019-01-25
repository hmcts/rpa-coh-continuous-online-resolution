package uk.gov.hmcts.reform.coh.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class ThreadPoolConfig {

    static private final Logger log = LoggerFactory.getLogger(ThreadPoolConfig.class);
    static private int errorCount;

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