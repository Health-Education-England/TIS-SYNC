package uk.nhs.tis.sync.job;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
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

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.tis.sync.dto.AmazonSqsMessageDto;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.dto.PostDataDmsDto;
import uk.nhs.tis.sync.service.DataRequestService;
import uk.nhs.tis.sync.service.DmsRecordAssembler;
import uk.nhs.tis.sync.service.KinesisService;

@ExtendWith(MockitoExtension.class)
class RecordResendingJobTest {

  private static final String QUEUE_URL = "mock-queue-url";

  private static final String STREAM_NAME = "streamName";

  private RecordResendingJob job;

  private PostDTO postDto;

  private RecordResendingJob jobSpy;

  @Mock
  private KinesisService kinesisServiceMock;

  @Mock
  private DataRequestService dataRequestServiceMock;

  @Mock
  private AmazonSQS amazonSqsMock;

  @Mock
  private DmsRecordAssembler dmsRecordAssemblerMock;

  private DmsDto dmsDto;

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

    postDto = new PostDTO();
    postDto.setId(44381L);
    postDto.setNationalPostNumber("EAN/8EJ83/094/SPR/001");
    postDto.status(Status.CURRENT);
    postDto.employingBodyId(287L);
    postDto.trainingBodyId(1464L);
    postDto.newPost(newPost);
    postDto.oldPost(null);
    postDto.owner("Health Education England North West London");
    postDto.intrepidId("128374444");

    PostDataDmsDto postDataDmsDto = new PostDataDmsDto();
    postDataDmsDto.setId("44381");
    postDataDmsDto.setNationalPostNumber("EAN/8EJ83/094/SPR/001");
    postDataDmsDto.setStatus("CURRENT");
    postDataDmsDto.setEmployingBodyId("287");
    postDataDmsDto.setTrainingBodyId("1464");
    postDataDmsDto.setOldPostId(null);
    postDataDmsDto.setNewPostId("184668");
    postDataDmsDto.setOwner("Health Education England North West London");
    postDataDmsDto.setIntrepidId("128374444");

    MetadataDto metadataDto = new MetadataDto();
    metadataDto.setTimestamp("timestamp");
    metadataDto.setRecordType("data");
    metadataDto.setOperation("load");
    metadataDto.setPartitionKeyType("schema-table");
    metadataDto.setSchemaName("tcs");
    metadataDto.setTableName("Post");
    metadataDto.setTransactionId("transaction-id");

    dmsDto = new DmsDto(postDataDmsDto, metadataDto);

    message1 = new Message();
    message1.setReceiptHandle("message1");
    message1.setBody("{" +
        "\"table\": \"Post\"," +
        "\"id\": \"44381\"" +
        "}"
    );
    message2 = new Message();
    message2.setReceiptHandle("message2");
    message2.setBody("{" +
        "\"table\": \"Post\"," +
        "\"id\": \"44382\"" +
        "}"
    );

    ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult()
        .withMessages(message1, message2);

    ReceiveMessageRequest request = new ReceiveMessageRequest()
        .withQueueUrl(QUEUE_URL)
        .withMaxNumberOfMessages(10);
    when(amazonSqsMock.receiveMessage(request)).thenReturn(receiveMessageResult);
  }

  @Test
  void shouldSendDataToKinesisStream() {
    when(dataRequestServiceMock.retrieveDto(any(AmazonSqsMessageDto.class))).thenReturn(postDto);
    when(dmsRecordAssemblerMock.assembleDmsDto(postDto)).thenReturn(dmsDto);

    job.run();

    verify(dataRequestServiceMock, times(2)).retrieveDto(any(AmazonSqsMessageDto.class));
    verify(dmsRecordAssemblerMock, times(2)).assembleDmsDto(postDto);

    ArgumentCaptor<List<DmsDto>> captor = ArgumentCaptor.forClass(List.class);
    verify(kinesisServiceMock).sendData(eq(STREAM_NAME), captor.capture());

    List<DmsDto> dmsDtos = captor.getValue();
    assertThat(dmsDtos.size(), is(2));
    assertThat(dmsDtos.get(0), instanceOf(DmsDto.class));
    assertThat(dmsDtos.get(1), instanceOf(DmsDto.class));
  }

  @Test
  void shouldDeleteProcessedMessages() {
    when(dataRequestServiceMock.retrieveDto(any(AmazonSqsMessageDto.class))).thenReturn(postDto);
    when(dmsRecordAssemblerMock.assembleDmsDto(postDto)).thenReturn(dmsDto);

    job.run();

    ArgumentCaptor<DeleteMessageBatchRequest> captor = ArgumentCaptor.forClass(
        DeleteMessageBatchRequest.class);
    verify(amazonSqsMock).deleteMessageBatch(captor.capture());

    DeleteMessageBatchRequest deleteRequest = captor.getValue();
    assertThat("Unexpected queue.", deleteRequest.getQueueUrl(), is(QUEUE_URL));

    List<DeleteMessageBatchRequestEntry> entries = deleteRequest.getEntries();
    assertThat("Unexpected number of entries.", entries.size(), is(2));

    DeleteMessageBatchRequestEntry entry = entries.get(0);
    assertThat("Unexpected id.", entry.getId(), is("0"));
    assertThat("Unexpected receipt handle.", entry.getReceiptHandle(), is("message1"));

    entry = entries.get(1);
    assertThat("Unexpected id.", entry.getId(), is("1"));
    assertThat("Unexpected receipt handle.", entry.getReceiptHandle(), is("message2"));
  }

  @Test
  void runSyncHandlingJobShouldBeCalled() {
    reset(amazonSqsMock);
    jobSpy = spy(job);
    jobSpy.recordResendingJob();
    verify(jobSpy).runRecordResendingJob();
  }

  @Test
  void shouldProcessRemainingMessagesWhenExceptionThrown() throws JsonProcessingException {
    ObjectMapper objectMapperMock = mock(ObjectMapper.class);
    when(objectMapperMock.readValue(message1.getBody(), AmazonSqsMessageDto.class))
        .thenThrow(JsonProcessingException.class);
    when(objectMapperMock.readValue(message2.getBody(), AmazonSqsMessageDto.class))
        .thenReturn(new AmazonSqsMessageDto("Post", "44382"));

    when(dataRequestServiceMock.retrieveDto(any(AmazonSqsMessageDto.class))).thenReturn(postDto);
    when(dmsRecordAssemblerMock.assembleDmsDto(postDto)).thenReturn(dmsDto);

    RecordResendingJob job = new RecordResendingJob(kinesisServiceMock,
        dataRequestServiceMock,
        objectMapperMock,
        amazonSqsMock,
        QUEUE_URL,
        dmsRecordAssemblerMock,
        STREAM_NAME);

    job.run();

    verify(dataRequestServiceMock).retrieveDto(any(AmazonSqsMessageDto.class));
    verify(dmsRecordAssemblerMock).assembleDmsDto(postDto);

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

    verify(dmsRecordAssemblerMock, never()).assembleDmsDto(any());
    verify(kinesisServiceMock, never()).sendData(any(), any());
  }
}
