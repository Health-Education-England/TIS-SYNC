package uk.nhs.tis.sync.config;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonKinesisConfiguration {
  /**
   * Configuration for an Amazon Kinesis bean.
   * @return an Amazon Kinesis object, necessary to send data into a Kinesis stream.
   */
  @Bean
  public AmazonKinesis amazonKinesis() {
    return AmazonKinesisClientBuilder.defaultClient();
  }
}
