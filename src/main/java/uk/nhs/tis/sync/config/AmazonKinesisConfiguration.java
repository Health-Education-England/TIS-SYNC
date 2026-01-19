package uk.nhs.tis.sync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.kinesis.KinesisClient;

@Configuration
public class AmazonKinesisConfiguration {
  /**
   * Configuration for an Amazon Kinesis bean.
   * @return an Amazon Kinesis object, necessary to send data into a Kinesis stream.
   */
  @Bean
  public KinesisClient amazonKinesis() {
    return KinesisClient.create();
  }
}
