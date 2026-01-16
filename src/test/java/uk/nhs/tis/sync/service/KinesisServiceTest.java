package uk.nhs.tis.sync.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.transformuk.hee.tis.tcs.api.dto.ContactDetailsDTO;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.dto.PostDmsDto;
import uk.nhs.tis.sync.dto.TrustDmsDto;

@ExtendWith(MockitoExtension.class)
class KinesisServiceTest {

  public static final String STREAM_NAME = "streamName";
  private List<DmsDto> dmsDtoList;

  private String timestamp;

  @Mock
  private KinesisClient mAmazonKinesis;

  @Spy
  @InjectMocks
  private KinesisService kinesisService;

  @BeforeEach
  void setUp() {
    PostDmsDto postDmsDto = new PostDmsDto();
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

    TrustDmsDto trustDmsDto = new TrustDmsDto();
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
  void givenAListOfDmsDtosItShouldSendThemSerializedIntoAStream() {
    kinesisService.sendData(STREAM_NAME, dmsDtoList);

    ArgumentCaptor<PutRecordsRequest> captor = ArgumentCaptor.forClass(PutRecordsRequest.class);
    verify(mAmazonKinesis).putRecords(captor.capture());
    PutRecordsRequest putRecordsRequest = captor.getValue();
    List<PutRecordsRequestEntry> putRecordsRequestEntryList = putRecordsRequest.records();
    PutRecordsRequestEntry putRecordsRequestEntry1 = putRecordsRequestEntryList.get(0);
    byte[] entry1 = putRecordsRequestEntry1.data().asByteArray();
    String actualRecord1 = new String(entry1, StandardCharsets.ISO_8859_1);
    String actualRecord1PartitionKey = putRecordsRequestEntry1.partitionKey();

    PutRecordsRequestEntry putRecordsRequestEntry2 = putRecordsRequestEntryList.get(1);
    byte[] entry2 = putRecordsRequestEntry2.data().asByteArray();
    String actualRecord2 = new String(entry2, StandardCharsets.ISO_8859_1);
    String actualRecord2PartitionKey = putRecordsRequestEntry2.partitionKey();

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

    assertThat("Unexpected record 1 partition key.", actualRecord1PartitionKey,
        is("tcs.Post"));
    assertThat("Unexpected record 2 partition key.", actualRecord2PartitionKey,
        is("reference.Trust"));
  }

  @Test
  void shouldCatchAJsonProcessingExceptionIfThrownByObjectMapper() {
    List<DmsDto> dmsDtoList = Collections.singletonList(new DmsDto(new Object(), null));
    assertDoesNotThrow(() -> kinesisService.sendData(STREAM_NAME, dmsDtoList));
    verifyNoInteractions(mAmazonKinesis);
  }

  @Test
  void shouldConvertNumbersToStrings() throws IOException {
    ContactDetailsDTO contactDetails = new ContactDetailsDTO();
    contactDetails.setId(10L);
    DmsDto dmsDto = new DmsDto(contactDetails, new MetadataDto());

    kinesisService.sendData(STREAM_NAME, Collections.singletonList(dmsDto));

    ArgumentCaptor<PutRecordsRequest> captor = ArgumentCaptor.forClass(PutRecordsRequest.class);
    verify(mAmazonKinesis).putRecords(captor.capture());

    PutRecordsRequest request = captor.getValue();
    byte[] data = request.records().get(0).data().asByteArray();
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(data);
    JsonNode idNode = jsonNode.get("data").get("id");

    assertThat("Unexpected node type.", idNode.getNodeType(), is(JsonNodeType.STRING));
    assertThat("Unexpected id value.", idNode.textValue(), is("10"));
  }

  @Test
  void shouldConvertDateToString() throws IOException {
    LocalDateTime now = LocalDateTime.now();
    ContactDetailsDTO contactDetails = new ContactDetailsDTO();
    contactDetails.setId(10L);
    contactDetails.setAmendedDate(now);
    DmsDto dmsDto = new DmsDto(contactDetails, new MetadataDto());

    kinesisService.sendData(STREAM_NAME, Collections.singletonList(dmsDto));

    ArgumentCaptor<PutRecordsRequest> captor = ArgumentCaptor.forClass(PutRecordsRequest.class);
    verify(mAmazonKinesis).putRecords(captor.capture());

    PutRecordsRequest request = captor.getValue();
    byte[] data = request.records().get(0).data().asByteArray();
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(data);
    JsonNode amendedDateNode = jsonNode.get("data").get("amendedDate");

    assertThat("Unexpected node type.", amendedDateNode.getNodeType(), is(JsonNodeType.STRING));
    String nowString = now.format(DateTimeFormatter.ISO_DATE_TIME);
    assertThat("Unexpected date value.", amendedDateNode.textValue(), is(nowString));
  }
}
