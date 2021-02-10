package uk.nhs.tis.sync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.nhs.tis.sync.service.DmsRecordAssembler;

@Configuration
public class DmsRecordAssemblerConfiguration {

  @Bean
  public DmsRecordAssembler dmsRecordAssembler() {
    return new DmsRecordAssembler();
  }
}
