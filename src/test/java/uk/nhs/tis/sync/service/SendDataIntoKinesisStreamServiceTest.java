package uk.nhs.tis.sync.service;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
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
public class SendDataIntoKinesisStreamServiceTest {

  PostDTO postDTO;

  TrustDTO trustDTO;

  ObjectMapper objectMapper;

  @Mock
  private AmazonKinesis amazonKinesisMock;

  @Spy
  @InjectMocks
  SendDataIntoKinesisStreamService testObj;

  @Before
  public void setUp() {
    postDTO = new PostDTO();
    trustDTO = new TrustDTO();
    objectMapper = new ObjectMapper();
  }

  @Test
  public void amazonKinesisShouldSendRecords() {
    testObj.sendData(postDTO, "Post");
    verify(amazonKinesisMock, times(1)).putRecords(any(PutRecordsRequest.class));
  }

  @Test
  public void amazonKinesisShouldSendAPutRecordsRequest() {
    testObj.sendData(postDTO, "Post");
    verify(amazonKinesisMock).putRecords(argThat(argument -> {
      assertThat(argument).isNotNull();
      assertThat(argument).isInstanceOf(PutRecordsRequest.class);
      return true;
    }));
  }

  @Test
  public void dataSentShouldBeFormattedCorrectly() throws JsonProcessingException {
    ArgumentCaptor<PutRecordsRequest> captor = ArgumentCaptor.forClass(PutRecordsRequest.class);
    testObj.sendData(postDTO, "Post");

    verify(amazonKinesisMock).putRecords(captor.capture());
    PutRecordsRequest putRecordsRequest = captor.getValue();
    ByteBuffer bytesBeingSent = putRecordsRequest.getRecords().get(0).getData();
    String stringBeingSent = StandardCharsets.ISO_8859_1.decode(bytesBeingSent).toString();

    Map<String, String> mapOfJsonBeingSent = objectMapper.readValue(stringBeingSent, Map.class);

    assertThat(mapOfJsonBeingSent).containsKeys("data", "metadata");
  }

  @Test
  public void metadataShouldBeCorrect() throws JsonProcessingException {
    ArgumentCaptor<PutRecordsRequest> captor = ArgumentCaptor.forClass(PutRecordsRequest.class);
    testObj.sendData(trustDTO, "Trust");

    verify(amazonKinesisMock).putRecords(captor.capture());
    PutRecordsRequest putRecordsRequest = captor.getValue();
    ByteBuffer bytesBeingSent = putRecordsRequest.getRecords().get(0).getData();
    String stringBeingSent = StandardCharsets.ISO_8859_1.decode(bytesBeingSent).toString();

    assertThat(stringBeingSent).contains("\"table\":\"Trust\"");
    assertThat(stringBeingSent).contains("\"schema\":\"reference\"");
  }
}
