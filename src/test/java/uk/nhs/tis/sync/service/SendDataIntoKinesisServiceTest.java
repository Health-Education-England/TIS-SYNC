package uk.nhs.tis.sync.service;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SendDataIntoKinesisServiceTest {

  PostDTO dto;

  @Mock
  private AmazonKinesis amazonKinesisMock;

  @Spy
  @InjectMocks
  SendDataIntoKinesisService testObj;

  @BeforeEach
  public void setUp() {
    dto = new PostDTO();
  }

  @Test
  public void amazonKinesisShouldSendRecords() {
    testObj.sendDataIntoKinesisStream(dto);
    verify(amazonKinesisMock, times(1)).putRecords(any(PutRecordsRequest.class));
  }

  @Test
  public void amazonKinesisShouldSendDataInCorrectFormat() {
    PutRecordsRequest putRecordsRequest = new PutRecordsRequest();
    testObj.sendDataIntoKinesisStream(dto);
    verify(amazonKinesisMock).putRecords(argThat(argument -> {
      assertThat(argument).isNotNull();
      return true;
    }));
  }
}
