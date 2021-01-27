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

  @Value("${application.aws.access.key.id}")
  private String awsAccessKeyId;

  @Value("${application.aws.secret.access.key}")
  private String awsSecretAccessKey;

  @Value("${application.aws.region}")
  private String region;

  AWSCredentialsProvider credentialsProvider() {
    BasicAWSCredentials basic = new BasicAWSCredentials(this.awsAccessKeyId,
        this.awsSecretAccessKey);
    return new AWSStaticCredentialsProvider(basic);
  }

  /**
   *
   * @return an Amazon Kinesis object, necessary to send data into a Kinesis stream.
   */
  @Bean
  public AmazonKinesis amazonKinesis() {
    AmazonKinesisClientBuilder clientBuilder =  AmazonKinesisClientBuilder.standard();
    clientBuilder.setRegion(region);
    clientBuilder.setCredentials(credentialsProvider());
    clientBuilder.setClientConfiguration(new ClientConfiguration());
    return clientBuilder.build();
  }

}
