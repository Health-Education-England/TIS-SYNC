package uk.nhs.tis.sync.config;

import com.transformuk.hee.tis.reference.client.config.ReferenceClientConfig;
import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@Profile({"dev", "stage", "prod", "uidev"})
public class TisClientProdConfig extends ReferenceClientConfig {

  @Bean
  public RestTemplate referenceRestTemplate(Keycloak keycloak) {
    return super.prodReferenceRestTemplate(keycloak);
  }

}
