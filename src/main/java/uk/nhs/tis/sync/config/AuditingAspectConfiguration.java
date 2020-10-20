package uk.nhs.tis.sync.config;

import com.transformuk.hee.tis.audit.repository.TisAuditRepository;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AuditingAspectConfiguration {

  @Bean
  public AuditEventRepository auditEventRepository() {
    return new TisAuditRepository();
  }
}
