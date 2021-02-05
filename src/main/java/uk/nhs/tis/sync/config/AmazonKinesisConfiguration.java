package uk.nhs.tis.sync.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmazonKinesisConfiguration {

  /**
   * @return an Amazon Kinesis object, necessary to send data into a Kinesis stream.
   */
  @Bean
  public AmazonKinesis amazonKinesis() {
    return AmazonKinesisClientBuilder.defaultClient();
  }
}
