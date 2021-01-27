package uk.nhs.tis.sync.service;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@RunWith(MockitoJUnitRunner.class)
public class SendDataIntoKinesisServiceTest {

  PostDTO dto;

  ObjectMapper objectMapper;

  @Mock
  private AmazonKinesis amazonKinesisMock;

  @Spy
  @InjectMocks
  SendDataIntoKinesisService testObj;

  @Before
  public void setUp() {
    dto = new PostDTO();
    objectMapper = new ObjectMapper();
  }

  @Test
  public void amazonKinesisShouldSendRecords() {
    testObj.sendDataIntoKinesisStream(dto);
    verify(amazonKinesisMock, times(1)).putRecords(any(PutRecordsRequest.class));
  }

  @Test
  public void amazonKinesisShouldSendAPutRecordsRequest() {
    PutRecordsRequest putRecordsRequest = new PutRecordsRequest();
    testObj.sendDataIntoKinesisStream(dto);
    verify(amazonKinesisMock).putRecords(argThat(argument -> {
      assertThat(argument).isNotNull();
      assertThat(argument).isInstanceOf(PutRecordsRequest.class);
      return true;
    }));
  }

  @Test
  public void dataSentShouldBeFormattedCorrectly() throws JsonProcessingException {
    ArgumentCaptor<PutRecordsRequest> captor = ArgumentCaptor.forClass(PutRecordsRequest.class);
    testObj.sendDataIntoKinesisStream(dto);

    verify(amazonKinesisMock).putRecords(captor.capture());
    PutRecordsRequest putRecordsRequest = captor.getValue();
    ByteBuffer bytesBeingSent = putRecordsRequest.getRecords().get(0).getData();
    String stringBeingSent = StandardCharsets.ISO_8859_1.decode(bytesBeingSent).toString();

    Map<String, String> mapOfJsonBeingSent = objectMapper.readValue(stringBeingSent, Map.class);

    assertThat(mapOfJsonBeingSent).containsKeys("data", "metadata");
  }
}
