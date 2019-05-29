package uk.nhs.tis.sync.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.ScheduledLockConfiguration;
import net.javacrumbs.shedlock.spring.ScheduledLockConfigurationBuilder;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.time.Duration;

@Configuration
@EnableSchedulerLock(defaultLockAtLeastFor = "PT1M", defaultLockAtMostFor = "PT1M")
public class ScheduledConfig {

  @Bean
  public ScheduledLockConfiguration taskScheduler(LockProvider lockProvider) {
    return ScheduledLockConfigurationBuilder
        .withLockProvider(lockProvider)
        .withPoolSize(10)
        .withDefaultLockAtMostFor(Duration.ofMinutes(10))
        .build();
  }

  @Bean
  public LockProvider lockProvider(DataSource dataSource) {
    return new JdbcTemplateLockProvider(dataSource);
  }
}
