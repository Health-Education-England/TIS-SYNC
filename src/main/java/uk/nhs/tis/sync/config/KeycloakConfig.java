package uk.nhs.tis.sync.config;

import com.transformuk.hee.tis.security.config.KeycloakClientConfig;
import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"dev", "stage", "prod", "uidev"})
public class KeycloakConfig extends KeycloakClientConfig {

  @Bean
  public Keycloak keycloak() {
    return super.createKeycloak();
  }
}
