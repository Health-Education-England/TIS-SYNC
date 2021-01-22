package uk.nhs.tis.sync.config;

import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TcsServiceImplConfiguration {

  @Value("${tcs.client.rate.limit}")
  private Long standardRequestPerSecondLimit;

  @Value("${tcs.client.bulk.rate.limit}")
  private Long bulkRequestPerSecondLimit;

  @Bean
  public TcsServiceImpl tcsServiceImpl() {
    return new TcsServiceImpl(standardRequestPerSecondLimit, bulkRequestPerSecondLimit);
  }
}
