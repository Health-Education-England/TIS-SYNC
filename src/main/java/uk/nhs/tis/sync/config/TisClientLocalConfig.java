package uk.nhs.tis.sync.config;

import com.transformuk.hee.tis.reference.client.config.ReferenceClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile("local")
public class TisClientLocalConfig extends ReferenceClientConfig {

  @Bean
  public RestTemplate referenceRestTemplate() {
    return super.defaultReferenceRestTemplate();
  }

}
