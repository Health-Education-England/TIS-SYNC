package uk.nhs.tis.sync.config;

import org.junit.rules.TestRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.nhs.tis.sync.mapper.PostDtoToDataDmsDtoMapper;
import uk.nhs.tis.sync.mapper.TrustDtoToDataDmsDtoMapper;
import uk.nhs.tis.sync.service.DmsRecordAssembler;

@Configuration
public class DmsRecordAssemblerConfiguration {

  private PostDtoToDataDmsDtoMapper postDtoToDataDmsDtoMapper;

  private TrustDtoToDataDmsDtoMapper trustDtoToDataDmsDtoMapper;

  @Bean
  public DmsRecordAssembler dmsRecordAssembler() {
    return new DmsRecordAssembler();
  }
}
