package uk.nhs.tis.sync.config;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import lombok.extern.slf4j.XSlf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.CredentialsProvider;
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
    String secretFirst10 = StringUtils.isBlank(this.awsSecretAccessKey) ? null : this.awsSecretAccessKey.substring(0, 10);
    BasicAWSCredentials basic = new BasicAWSCredentials(this.awsAccessKeyId,
      this.awsSecretAccessKey);
    return new AWSStaticCredentialsProvider(basic);
  }

  @Bean
  public AmazonKinesis amazonKinesis() {
    AmazonKinesisClientBuilder clientBuilder =  AmazonKinesisClientBuilder.standard();
    clientBuilder.setRegion(region);
    clientBuilder.setCredentials(credentialsProvider());
    clientBuilder.setClientConfiguration(new ClientConfiguration());

    return clientBuilder.build();
  }

}
