package uk.nhs.tis.sync.config;

import com.hubspot.slack.client.SlackClient;
import com.hubspot.slack.client.SlackClientFactory;
import com.hubspot.slack.client.SlackClientRuntimeConfig;
import io.github.jhipster.config.JHipsterProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class MonitoringConfiguration {
  
  @Value("${slack.api.token}")
  private String SLACK_TOKEN;

  private final Logger log = LoggerFactory.getLogger(MonitoringConfiguration.class);

  private final JHipsterProperties jHipsterProperties;

  private Environment env;

  public MonitoringConfiguration(Environment env, JHipsterProperties jHipsterProperties) {
    this.env = env;
    this.jHipsterProperties = jHipsterProperties;
  }

  @Bean
  public SlackClient getSlackClient() {
    return SlackClientFactory.defaultFactory().build(getSlackConfig());
  }

  @Bean
  public SlackClientRuntimeConfig getSlackConfig() {
    return SlackClientRuntimeConfig.builder().setTokenSupplier(() -> SLACK_TOKEN).build();
  }
}
