package uk.nhs.tis.sync.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.dto.PostDataDmsDto;
import uk.nhs.tis.sync.dto.TrustDataDmsDto;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class KinesisServiceTest {

  private PostDataDmsDto postDataDmsDto;

  private TrustDataDmsDto trustDataDmsDto;

  public static final String STREAM_NAME = "streamName";

  private List<DmsDto> dmsDtoList;

  private ObjectMapper objectMapper;

  private String timestamp;

  @Mock
  private AmazonKinesis mAmazonKinesis;

  @Spy
  @InjectMocks
  private KinesisService kinesisService;

  @Before
  public void setUp() {
    objectMapper = new ObjectMapper();

    postDataDmsDto = new PostDataDmsDto("44381",
        "EAN/8EJ83/094/SPR/001",
        "CURRENT",
        "287",
        "1464",
        null,
        "184668",
        "Health Education England North West London",
        "128374444"
        );

    timestamp = Instant.now().toString();

    MetadataDto metadataDto1 = new MetadataDto(timestamp,
        "data",
        "load",
        "schema-table",
        "tcs",
        "Post",
        "000");

    DmsDto dmsDto1 = new DmsDto(postDataDmsDto, metadataDto1);

    trustDataDmsDto = new TrustDataDmsDto("222",
        "someLocalOffice",
        "CURRENT",
        "trustKnownAs",
        "trustName",
        "000",
        "3",
        "1");

    MetadataDto metadataDto2 = new MetadataDto(timestamp,
        "data",
        "load",
        "schema-table",
        "reference",
        "Trust",
        "111");

    DmsDto dmsDto2 = new DmsDto(trustDataDmsDto, metadataDto2);

    dmsDtoList = new ArrayList<>();
    dmsDtoList.add(dmsDto1);
    dmsDtoList.add(dmsDto2);
  }

  @Test
  public void givenAListOfDmsDtosItShouldSendThemSerializedIntoAStream() throws JsonProcessingException {
    kinesisService.sendData(STREAM_NAME, dmsDtoList);

    ArgumentCaptor<PutRecordsRequest> captor = ArgumentCaptor.forClass(PutRecordsRequest.class);
    verify(mAmazonKinesis).putRecords(captor.capture());
    PutRecordsRequest putRecordsRequest = captor.getValue();
    List<PutRecordsRequestEntry> putRecordsRequestEntryList = putRecordsRequest.getRecords();
    PutRecordsRequestEntry putRecordsRequestEntry1 = putRecordsRequestEntryList.get(0);
    byte[] entry1 = putRecordsRequestEntry1.getData().array();
    String actualRecord1 = new String(entry1, StandardCharsets.ISO_8859_1);

    PutRecordsRequestEntry putRecordsRequestEntry2 = putRecordsRequestEntryList.get(1);
    byte[] entry2 = putRecordsRequestEntry2.getData().array();
    String actualRecord2 = new String(entry2, StandardCharsets.ISO_8859_1);

    Map<String, String> actualRecordMap1 = objectMapper.readValue(actualRecord1, Map.class);

    Map<String, String> actualRecordMap2 = objectMapper.readValue(actualRecord2, Map.class);

    String expectedRecord1 = "{\n" +
        "\"data\":\t{\n" +
        "\"id\":\t44381,\n" +
        "\"nationalPostNumber\":\t\"EAN/8EJ83/094/SPR/001\",\n" +
        "\"status\":\t\"CURRENT\",\n" +
        "\"employingBodyId\":\t287,\n" +
        "\"trainingBodyId\":\t1464,\n" +
        "\"newPostId\":\t184668,\n" +
        "\"owner\":\t\"Health Education England North West London\",\n" +
        "\"intrepidId\":\t\"128374444\"\n" +
        "},\n" +
        "\"metadata\":\t{\n" +
        "\"timestamp\":\t\"" + timestamp + "\",\n" +
        "\"record-type\":\t\"data\",\n" +
        "\"operation\":\t\"load\",\n" +
        "\"partition-key-type\":\t\"schema-table\",\n" +
        "\"schema-name\":\t\"tcs\",\n" +
        "\"table-name\":\t\"Post\",\n" +
        "\"transaction-id\":\t\"000\"\n" +
        "}\n" +
        "}";

    String expectedRecord2 = "{\n" +
        "\"data\":\t{\n" +
        "\"code\":\t\"222\",\n" +
        "\"localOffice\":\t\"someLocalOffice\",\n" +
        "\"status\":\t\"CURRENT\",\n" +
        "\"trustKnownAs\":\t\"trustKnownAs\",\n" +
        "\"trustName\":\t\"trustName\",\n" +
        "\"trustNumber\":\t\"000\",\n" +
        "\"intrepidId\":\t\"3\",\n" +
        "\"id\":\t1\n" +
        "},\n" +
        "\"metadata\":\t{\n" +
        "\"timestamp\":\t\"" + timestamp + "\",\n" +
        "\"record-type\":\t\"data\",\n" +
        "\"operation\":\t\"load\",\n" +
        "\"partition-key-type\":\t\"schema-table\",\n" +
        "\"schema-name\":\t\"reference\",\n" +
        "\"table-name\":\t\"Trust\",\n" +
        "\"transaction-id\":\t111\n" +
        "}\n" +
        "}";

    Map<String, String> expectedRecordMap1 = objectMapper.readValue(expectedRecord1, Map.class);

    Map<String, String> expectedRecordMap2 = objectMapper.readValue(expectedRecord2, Map.class);

    assertEquals(expectedRecordMap1.toString(), actualRecordMap1.toString());
    assertEquals(expectedRecordMap2.toString(), actualRecordMap2.toString());
  }

  @Test
  public void shouldCatchAJsonProcessingExceptionIfThrownByObjectMapper() throws JsonProcessingException {
    ObjectMapper mObjectMapper = mock(ObjectMapper.class);
    kinesisService.setObjectMapper(mObjectMapper);
    when(mObjectMapper.writeValueAsString(any(DmsDto.class))).thenThrow(JsonProcessingException.class);

    assertDoesNotThrow(() -> kinesisService.sendData(STREAM_NAME, dmsDtoList));
  }

  @Test
  public void getObjectMapperGetsTheObjectMapper() {
    ObjectMapper objectMapper = kinesisService.getObjectMapper();
    assertThat(objectMapper).isNotNull().isInstanceOf(ObjectMapper.class);
  }

  @Test
  public void setObjectMapperSetsTheObjectMapper() {
    ObjectMapper originalObjectMapper = kinesisService.getObjectMapper();
    ObjectMapper newObjectMapper = new ObjectMapper();
    kinesisService.setObjectMapper(newObjectMapper);

    assertThat(originalObjectMapper).isNotEqualTo(kinesisService.getObjectMapper());
    assertThat(newObjectMapper).isEqualTo(kinesisService.getObjectMapper());
  }
}
