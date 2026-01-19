package uk.nhs.tis.sync.job;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.dto.PostDmsDto;
import uk.nhs.tis.sync.dto.ProgrammeDmsDto;
import uk.nhs.tis.sync.service.DataRequestService;
import uk.nhs.tis.sync.service.DmsRecordAssembler;
import uk.nhs.tis.sync.service.KinesisService;

@ExtendWith(MockitoExtension.class)
class RecordResendingJobTest {

  private static final String QUEUE_URL = "mock-queue-url";

  private static final String STREAM_NAME = "streamName";

  private static final String TIS_TRIGGER = "trigger";
  private static final String TIS_TRIGGER_DETAIL = "details";

  private RecordResendingJob job;

  private List<Object> postDtos;

  @Mock
  private KinesisService kinesisServiceMock;

  @Mock
  private DataRequestService dataRequestServiceMock;

  @Mock
  private SqsClient amazonSqsMock;

  @Mock
  private DmsRecordAssembler dmsRecordAssemblerMock;

  private List<DmsDto> dmsDtos;

  private Message message1;
  private Message message2;

  @BeforeEach
  void setUp() {
    job = new RecordResendingJob(kinesisServiceMock,
        dataRequestServiceMock,
        new ObjectMapper(),
        amazonSqsMock,
        QUEUE_URL,
        dmsRecordAssemblerMock,
        STREAM_NAME);

    PostDTO newPost = new PostDTO();
    newPost.setId(184668L);

    PostDTO postDto = new PostDTO();
    postDto.setId(44381L);
    postDto.setNationalPostNumber("EAN/8EJ83/094/SPR/001");
    postDto.status(Status.CURRENT);
    postDto.employingBodyId(287L);
    postDto.trainingBodyId(1464L);
    postDto.newPost(newPost);
    postDto.oldPost(null);
    postDto.owner("Health Education England North West London");
    postDto.intrepidId("128374444");
    postDtos = Collections.singletonList(postDto);

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

    MetadataDto metadataDto = new MetadataDto();
    metadataDto.setTimestamp("timestamp");
    metadataDto.setRecordType("data");
    metadataDto.setOperation("load");
    metadataDto.setPartitionKeyType("schema-table");
    metadataDto.setSchemaName("tcs");
    metadataDto.setTableName("Post");
    metadataDto.setTransactionId("transaction-id");

    dmsDtos = Collections.singletonList(new DmsDto(postDmsDto, metadataDto));

    message1 = Message.builder().receiptHandle("message1").body("{" +
        "\"table\": \"Post\"," +
        "\"id\": \"44381\"," +
        "\"tisTrigger\": \"" + TIS_TRIGGER + "\"," +
        "\"tisTriggerDetail\": \"" + TIS_TRIGGER_DETAIL + "\"" +
        "}").build();
    message2 = Message.builder().receiptHandle("message2").body("{" +
        "\"table\": \"Post\"," +
        "\"id\": \"44382\"" +
        "}").build();

    ReceiveMessageResponse receiveMessageResult = ReceiveMessageResponse.builder()
        .messages(message1, message2)
        .build();

    ReceiveMessageRequest request = ReceiveMessageRequest.builder()
        .queueUrl(QUEUE_URL)
        .maxNumberOfMessages(10)
        .build();
    when(amazonSqsMock.receiveMessage(request)).thenReturn(receiveMessageResult);
  }

  @Test
  void shouldSendDataToKinesisStream() {
    when(dataRequestServiceMock.retrieveDtos(any(Map.class))).thenReturn(postDtos);
    when(dmsRecordAssemblerMock.assembleDmsDtos(postDtos, TIS_TRIGGER, TIS_TRIGGER_DETAIL))
        .thenReturn(dmsDtos);
    when(dmsRecordAssemblerMock.assembleDmsDtos(postDtos, null, null))
        .thenReturn(dmsDtos);

    job.run();

    verify(dataRequestServiceMock, times(2))
        .retrieveDtos(any(Map.class));
    verify(dmsRecordAssemblerMock)
        .assembleDmsDtos(postDtos, TIS_TRIGGER, TIS_TRIGGER_DETAIL);
    verify(dmsRecordAssemblerMock)
        .assembleDmsDtos(postDtos, null, null);

    ArgumentCaptor<List<DmsDto>> captor = ArgumentCaptor.forClass(List.class);
    verify(kinesisServiceMock).sendData(eq(STREAM_NAME), captor.capture());

    List<DmsDto> dmsDtos = captor.getValue();
    assertThat(dmsDtos.size(), is(2));
    assertThat(dmsDtos.get(0), instanceOf(DmsDto.class));
    assertThat(dmsDtos.get(1), instanceOf(DmsDto.class));
  }

  @Test
  void shouldDeleteProcessedMessages() {
    when(dataRequestServiceMock.retrieveDtos(any(Map.class))).thenReturn(postDtos);
    when(dmsRecordAssemblerMock.assembleDmsDtos(eq(postDtos), any(), any())).thenReturn(dmsDtos);

    job.run();

    ArgumentCaptor<DeleteMessageBatchRequest> captor = ArgumentCaptor.forClass(
        DeleteMessageBatchRequest.class);
    verify(amazonSqsMock).deleteMessageBatch(captor.capture());

    DeleteMessageBatchRequest deleteRequest = captor.getValue();
    assertThat("Unexpected queue.", deleteRequest.queueUrl(), is(QUEUE_URL));

    List<DeleteMessageBatchRequestEntry> entries = deleteRequest.entries();
    assertThat("Unexpected number of entries.", entries.size(), is(2));

    DeleteMessageBatchRequestEntry entry = entries.get(0);
    assertThat("Unexpected id.", entry.id(), is("0"));
    assertThat("Unexpected receipt handle.", entry.receiptHandle(), is("message1"));

    entry = entries.get(1);
    assertThat("Unexpected id.", entry.id(), is("1"));
    assertThat("Unexpected receipt handle.", entry.receiptHandle(), is("message2"));
  }

  @Test
  void runSyncHandlingJobShouldBeCalled() {
    reset(amazonSqsMock);
    RecordResendingJob jobSpy = spy(job);
    jobSpy.recordResendingJob();
    verify(jobSpy).runRecordResendingJob();
  }

  @Test
  void shouldProcessRemainingMessagesWhenExceptionThrown() throws JsonProcessingException {
    ObjectMapper objectMapperMock = mock(ObjectMapper.class);
    when(objectMapperMock.readValue(message1.body(), Map.class))
        .thenThrow(JsonProcessingException.class);
    when(objectMapperMock.readValue(message2.body(), Map.class))
        .thenReturn(new HashMap<String, String>() {{
          put("Post", "44382");
        }});

    when(dataRequestServiceMock.retrieveDtos(any(Map.class))).thenReturn(postDtos);
    when(dmsRecordAssemblerMock.assembleDmsDtos(eq(postDtos), any(), any())).thenReturn(dmsDtos);

    RecordResendingJob job = new RecordResendingJob(kinesisServiceMock,
        dataRequestServiceMock,
        objectMapperMock,
        amazonSqsMock,
        QUEUE_URL,
        dmsRecordAssemblerMock,
        STREAM_NAME);

    job.run();

    verify(dataRequestServiceMock).retrieveDtos(any(Map.class));
    verify(dmsRecordAssemblerMock).assembleDmsDtos(eq(postDtos), any(), any());

    ArgumentCaptor<List<DmsDto>> captor = ArgumentCaptor.forClass(List.class);
    verify(kinesisServiceMock).sendData(eq(STREAM_NAME), captor.capture());

    List<DmsDto> dmsDtos = captor.getValue();
    assertThat(dmsDtos.size(), is(1));
    assertThat(dmsDtos.get(0), instanceOf(DmsDto.class));
  }

  @Test
  void shouldNotTryToSyncARecordIfTheRequestedDtoIsNotFound() {
//  DataRequestServiceMock.retrieveDto isn't stubbed in this test method, therefore will return
//  null when fetching a dto (i.e. dto not found)

    job.run();

    verify(dmsRecordAssemblerMock, never()).assembleDmsDtos(any(), any(), any());
    verify(kinesisServiceMock, never()).sendData(any(), any());
  }

  @Test
  void shouldHandleMessagesResultingInMultipleDtos() {
    PostDmsDto postDto1 = new PostDmsDto();
    postDto1.setId("1");
    PostDmsDto postDto2 = new PostDmsDto();
    postDto2.setId("2");
    List<Object> postDtos = Arrays.asList(postDto1, postDto2);

    ProgrammeDmsDto programmeDto = new ProgrammeDmsDto();
    programmeDto.setId("3");
    List<Object> programmeDtos = Collections.singletonList(programmeDto);

    when(dataRequestServiceMock.retrieveDtos(any(Map.class))).thenReturn(postDtos, programmeDtos);

    DmsDto dmsDto1 = new DmsDto(null, null);
    DmsDto dmsDto2 = new DmsDto(null, null);
    when(dmsRecordAssemblerMock.assembleDmsDtos(eq(postDtos), any(), any())).thenReturn(
        Arrays.asList(dmsDto1, dmsDto2));

    DmsDto dmsDto3 = new DmsDto(null, null);
    when(dmsRecordAssemblerMock.assembleDmsDtos(eq(programmeDtos), any(), any())).thenReturn(
        Collections.singletonList(dmsDto3));

    job.run();

    ArgumentCaptor<List<DmsDto>> captor = ArgumentCaptor.forClass(List.class);
    verify(kinesisServiceMock).sendData(eq(STREAM_NAME), captor.capture());

    List<DmsDto> dmsDtos = captor.getValue();
    assertThat(dmsDtos.size(), is(3));
    assertThat(dmsDtos.get(0), sameInstance(dmsDto1));
    assertThat(dmsDtos.get(1), sameInstance(dmsDto2));
    assertThat(dmsDtos.get(2), sameInstance(dmsDto3));
  }
}
