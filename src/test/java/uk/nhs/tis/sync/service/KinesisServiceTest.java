package uk.nhs.tis.sync.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.dto.PostDmsDto;
import uk.nhs.tis.sync.dto.TrustDmsDto;

@RunWith(MockitoJUnitRunner.class)
public class KinesisServiceTest {

  public static final String STREAM_NAME = "streamName";
  private PostDmsDto postDmsDto;
  private TrustDmsDto trustDmsDto;
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

    postDmsDto = new PostDmsDto();
    postDmsDto.setId("44381");
    postDmsDto.setNationalPostNumber("EAN/8EJ83/094/SPR/001");
    postDmsDto.setStatus("CURRENT");
    postDmsDto.setEmployingBodyId("287");
    postDmsDto.setTrainingBodyId("1464");
    postDmsDto.setOldPostId(null);
    postDmsDto.setNewPostId("184668");
    postDmsDto.setOwner("Health Education England North West London");
    postDmsDto.setIntrepidId("128374444");

    timestamp = Instant.now().toString();

    MetadataDto metadataDto1 = new MetadataDto();
    metadataDto1.setTimestamp(timestamp);
    metadataDto1.setRecordType("data");
    metadataDto1.setOperation("load");
    metadataDto1.setPartitionKeyType("schema-table");
    metadataDto1.setSchemaName("tcs");
    metadataDto1.setTableName("Post");
    metadataDto1.setTransactionId("000");

    DmsDto dmsDto1 = new DmsDto(postDmsDto, metadataDto1);

    trustDmsDto = new TrustDmsDto();
    trustDmsDto.setCode("222");
    trustDmsDto.setLocalOffice("someLocalOffice");
    trustDmsDto.setStatus("CURRENT");
    trustDmsDto.setTrustKnownAs("trustKnownAs");
    trustDmsDto.setTrustName("trustName");
    trustDmsDto.setTrustNumber("000");
    trustDmsDto.setIntrepidId("3");
    trustDmsDto.setId("1");

    MetadataDto metadataDto2 = new MetadataDto();
    metadataDto2.setTimestamp(timestamp);
    metadataDto2.setRecordType("data");
    metadataDto2.setOperation("load");
    metadataDto2.setPartitionKeyType("schema-table");
    metadataDto2.setSchemaName("reference");
    metadataDto2.setTableName("Trust");
    metadataDto2.setTransactionId("111");

    DmsDto dmsDto2 = new DmsDto(trustDmsDto, metadataDto2);

    dmsDtoList = new ArrayList<>();
    dmsDtoList.add(dmsDto1);
    dmsDtoList.add(dmsDto2);
  }

  @Test
  public void givenAListOfDmsDtosItShouldSendThemSerializedIntoAStream() {
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

    String expectedRecord1 = "{\n" +
        "\"data\":\t{\n" +
        "\"id\":\t\"44381\",\n" +
        "\"nationalPostNumber\":\t\"EAN/8EJ83/094/SPR/001\",\n" +
        "\"status\":\t\"CURRENT\",\n" +
        "\"employingBodyId\":\t\"287\",\n" +
        "\"trainingBodyId\":\t\"1464\",\n" +
        "\"newPostId\":\t\"184668\",\n" +
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
        "\"id\":\t\"1\"\n" +
        "},\n" +
        "\"metadata\":\t{\n" +
        "\"timestamp\":\t\"" + timestamp + "\",\n" +
        "\"record-type\":\t\"data\",\n" +
        "\"operation\":\t\"load\",\n" +
        "\"partition-key-type\":\t\"schema-table\",\n" +
        "\"schema-name\":\t\"reference\",\n" +
        "\"table-name\":\t\"Trust\",\n" +
        "\"transaction-id\":\t\"111\"\n" +
        "}\n" +
        "}";

    JSONObject actualJsonRecord1 = new JSONObject(actualRecord1);

    JSONObject actualJsonRecord2 = new JSONObject(actualRecord2);

    JSONObject expectedJsonRecord1 = new JSONObject(expectedRecord1);

    JSONObject expectedJsonRecord2 = new JSONObject(expectedRecord2);

    JSONAssert.assertEquals(expectedJsonRecord1, actualJsonRecord1, false);
    JSONAssert.assertEquals(expectedJsonRecord2, actualJsonRecord2, false);
  }

  @Test
  public void shouldCatchAJsonProcessingExceptionIfThrownByObjectMapper()
      throws JsonProcessingException {
    ObjectMapper mObjectMapper = mock(ObjectMapper.class);
    kinesisService.setObjectMapper(mObjectMapper);
    when(mObjectMapper.writeValueAsString(any(DmsDto.class)))
        .thenThrow(JsonProcessingException.class);

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
