package uk.nhs.tis.sync.config;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonSqsConfiguration {

  @Bean
  public AmazonSQS amazonSqs() {
    return AmazonSQSClientBuilder.defaultClient();
  }
}
