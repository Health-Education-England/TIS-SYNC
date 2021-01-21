package uk.nhs.tis.sync.config;

import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageListeningJobConfiguration {

  @Bean
  public TcsServiceImpl tcsServiceImpl() {
    return new TcsServiceImpl(100L, 10L);
  }
}
