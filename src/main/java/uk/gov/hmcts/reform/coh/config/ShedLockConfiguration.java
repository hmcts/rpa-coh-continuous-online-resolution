package uk.gov.hmcts.reform.coh.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.ScheduledLockConfiguration;
import net.javacrumbs.shedlock.spring.ScheduledLockConfigurationBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import javax.sql.DataSource;

@Configuration
@ConditionalOnProperty(value = "scheduling.enabled", matchIfMissing = true)
public class ShedLockConfiguration {

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }

    @Bean
    public ScheduledLockConfiguration taskScheduler(
        LockProvider lockProvider,
        TaskScheduler scheduler,
        @Value("${scheduling.lock_at_most_for}") String lockAtMostFor
    ) {
        return ScheduledLockConfigurationBuilder
            .withLockProvider(lockProvider)
            .withTaskScheduler(scheduler)
            .withDefaultLockAtMostFor(Duration.parse(lockAtMostFor))
            .build();
    }
}
