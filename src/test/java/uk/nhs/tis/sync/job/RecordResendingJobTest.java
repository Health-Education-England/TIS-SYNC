package uk.nhs.tis.sync.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
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

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RecordResendingJobTest {

  private static final String QUEUE_URL = "mock-queue-url";

  public static final String STREAM_NAME = "streamName";

  private RecordResendingJob job;

  private PostDTO postDto;

  private RecordResendingJob jobSpy;

  @Mock
  KinesisService kinesisServiceMock;

  @Mock
  DataRequestService dataRequestServiceMock;

  @Mock
  AmazonSQS amazonSqsMock;

  @Mock
  DmsRecordAssembler dmsRecordAssemblerMock;

  DmsDto dmsDto;

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

    PostDataDmsDto postDataDmsDto = new PostDataDmsDto("44381",
        "EAN/8EJ83/094/SPR/001",
        "CURRENT",
        "287",
        "1464",
        null,
        "184668",
        "Health Education England North West London",
        "128374444"
    );

    MetadataDto metadataDto = new MetadataDto("timestamp",
        "data",
        "load",
        "schema-table",
        "tcs",
        "Post",
        "transactionId");

    dmsDto = new DmsDto(postDataDmsDto, metadataDto);

    Message message = new Message();
    message.setBody("{" +
        "\"table\": \"Post\"," +
        "\"id\": \"44381\"" +
        "}"
    );
    ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult().withMessages(message);
    when(amazonSqsMock.receiveMessage(QUEUE_URL)).thenReturn(receiveMessageResult);
  }

  @Test
  void shouldSendDataToKinesisStream() {

    when(dataRequestServiceMock.retrieveDto(any(AmazonSqsMessageDto.class))).thenReturn(postDto);
    when(dmsRecordAssemblerMock.assembleDmsDto(postDto)).thenReturn(dmsDto);

    job.run();

    verify(dataRequestServiceMock).retrieveDto(any(AmazonSqsMessageDto.class));
    verify(dmsRecordAssemblerMock).assembleDmsDto(postDto);

    ArgumentCaptor<List<DmsDto>> captor = ArgumentCaptor.forClass(List.class);
    verify(kinesisServiceMock).sendData(eq(STREAM_NAME), captor.capture());
    assertThat(captor.getValue().get(0)).isInstanceOf(DmsDto.class);
  }

  @Test
  void runSyncHandlingJobShouldBeCalled() {
    reset(amazonSqsMock);
    jobSpy = spy(job);
    jobSpy.syncHandlingJob();
    verify(jobSpy).runSyncHandlingJob();
  }

  @Test
  void shouldCatchExceptionsWhenRunMethodIsCalled() throws JsonProcessingException {
    ObjectMapper objectMapperMock = mock(ObjectMapper.class);
    when(objectMapperMock.readValue(anyString(), eq(AmazonSqsMessageDto.class))).thenThrow(JsonProcessingException.class);

    //mock a list of messages (1 message in the list)
    ReceiveMessageResult receiveMessageResultMock = mock(ReceiveMessageResult.class);
    when(amazonSqsMock.receiveMessage(QUEUE_URL)).thenReturn(receiveMessageResultMock);
    List<Message> messages = new ArrayList<>();
    messages.add(new Message());
    when(receiveMessageResultMock.getMessages()).thenReturn(messages);

    job = new RecordResendingJob(kinesisServiceMock,
        dataRequestServiceMock,
        objectMapperMock,
        amazonSqsMock,
        QUEUE_URL,
        dmsRecordAssemblerMock,
        STREAM_NAME);

    job.run();

    Throwable throwable = catchThrowable(() -> job.run());
    assertThat(throwable).isNull();
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
