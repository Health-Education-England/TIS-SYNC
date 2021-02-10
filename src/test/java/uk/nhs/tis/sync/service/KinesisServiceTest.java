package uk.nhs.tis.sync.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class KinesisServiceTest {

  PostDTO postDTO;

  TrustDTO trustDTO;

  ObjectMapper objectMapper;

  @Mock
  private AmazonKinesis mAmazonKinesis;

  @Mock
  private DmsRecordAssembler mDmsRecordAssembler;

  @Spy
  @InjectMocks
  KinesisService testObj;

  @Before
  public void setUp() {
    postDTO = new PostDTO();
    trustDTO = new TrustDTO();
    objectMapper = new ObjectMapper();
  }

  @Test
  public void amazonKinesisShouldSendAPutRecordsRequest() throws JsonProcessingException {
    when(mDmsRecordAssembler.buildRecord(any())).thenReturn("jsonStringDmsDto");
    testObj.sendData(postDTO);
    verify(mAmazonKinesis).putRecords(argThat(argument -> {
      assertThat(argument).isNotNull();
      assertThat(argument).isInstanceOf(PutRecordsRequest.class);
      return true;
    }));
  }

//  @Test
//  public void dataSentShouldBeFormattedCorrectly() throws JsonProcessingException {
//    ArgumentCaptor<PutRecordsRequest> captor = ArgumentCaptor.forClass(PutRecordsRequest.class);
//    testObj.sendData(postDTO);
//
//    verify(mAmazonKinesis).putRecords(captor.capture());
//    PutRecordsRequest putRecordsRequest = captor.getValue();
//    ByteBuffer bytesBeingSent = putRecordsRequest.getRecords().get(0).getData();
//    String stringBeingSent = StandardCharsets.ISO_8859_1.decode(bytesBeingSent).toString();
//
//    Map<String, String> mapOfJsonBeingSent = objectMapper.readValue(stringBeingSent, Map.class);
//
//    String expectedJson = "{\n" +
//        "\"data\":\t{\n" +
//        "\"id\":\t44381,\n" +
//        "\"nationalPostNumber\":\t\"EAN/8EJ83/094/SPR/001\",\n" +
//        "\"status\":\t\"CURRENT\",\n" +
//        "\"employingBodyId\":\t287,\n" +
//        "\"trainingBodyId\":\t1464,\n" +
//        "\"newPostId\":\t184668,\n" +
//        "\"owner\":\t\"Health Education England North West London\",\n" +
//        "\"intrepidId\":\t\"128374444\",\n" +
//        "\"legacy\":\tfalse,\n" +
//        "\"bypassNPNGeneration\":\ttrue\n" +
//        "},\n" +
//        "\"metadata\":\t{\n" +
//        "\"timestamp\":\t\"2021-02-09T13:05:55.354299Z\",\n" +
//        "\"record-type\":\t\"data\",\n" +
//        "\"operation\":\t\"update\",\n" +
//        "\"partition-key-type\":\t\"schema-table\",\n" +
//        "\"schema-name\":\t\"tcs\",\n" +
//        "\"table-name\":\t\"Post\",\n" +
//        "\"transaction-id\":\t20302313979195\n" +
//        "}\n" +
//        "}";
//
//    Map<String, String> mapOfExpectedJson = objectMapper.readValue(expectedJson, Map.class);
//
//    assertThat(mapOfJsonBeingSent).containsKeys("data", "metadata");
//  }

//  @Test
//  public void metadataShouldBeConsistentWhenSendingAPost() {
//    ArgumentCaptor<PutRecordsRequest> captor = ArgumentCaptor.forClass(PutRecordsRequest.class);
//    testObj.sendData(postDTO);
//
//    verify(mAmazonKinesis).putRecords(captor.capture());
//    PutRecordsRequest putRecordsRequest = captor.getValue();
//    ByteBuffer bytesBeingSent = putRecordsRequest.getRecords().get(0).getData();
//    String stringBeingSent = StandardCharsets.ISO_8859_1.decode(bytesBeingSent).toString();
//
//    assertThat(stringBeingSent)
//        .contains("\"table\":\"Post\"")
//        .contains("\"schema\":\"tcs\"");
//  }
//
//  @Test
//  public void metadataShouldBeConsistentWhenSendingATrust() {
//    ArgumentCaptor<PutRecordsRequest> captor = ArgumentCaptor.forClass(PutRecordsRequest.class);
//    testObj.sendData(trustDTO);
//
//    verify(mAmazonKinesis).putRecords(captor.capture());
//    PutRecordsRequest putRecordsRequest = captor.getValue();
//    ByteBuffer bytesBeingSent = putRecordsRequest.getRecords().get(0).getData();
//    String stringBeingSent = StandardCharsets.ISO_8859_1.decode(bytesBeingSent).toString();
//
//    assertThat(stringBeingSent)
//        .contains("\"table\":\"Trust\"")
//        .contains("\"schema\":\"reference\"");
//  }

//  @Test
//  public void exceptionShouldBeCaughtIfObjectMapperThrowsOne() throws JsonProcessingException {
//    ObjectMapper objectMapperMock = mock(ObjectMapper.class);
//    doThrow(JsonProcessingException.class).when(objectMapperMock).writeValueAsString(any());
//
//    KinesisService testObj2 =
//        new KinesisService(mAmazonKinesis, mDmsDtoAssembler, "streamName");
//    testObj2.setObjectMapper(objectMapperMock);
//
//    Throwable throwable = catchThrowable(() -> testObj2.sendData(trustDTO));
//    assertThat(throwable).isNull();
//  }

  @Test
  public void getObjectMapperGetsTheObjectMapper() {
    ObjectMapper objectMapper = testObj.getObjectMapper();
    assertThat(objectMapper).isNotNull().isInstanceOf(ObjectMapper.class);
  }

  @Test
  public void setObjectMapperSetsTheObjectMapper() {
    ObjectMapper originalObjectMapper = testObj.getObjectMapper();
    ObjectMapper newObjectMapper = new ObjectMapper();
    testObj.setObjectMapper(newObjectMapper);

    assertThat(originalObjectMapper).isNotEqualTo(testObj.getObjectMapper());
    assertThat(newObjectMapper).isEqualTo(testObj.getObjectMapper());
  }
}
