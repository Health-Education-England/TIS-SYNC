package uk.nhs.tis.sync.job;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.tis.sync.dto.AmazonSqsMessageDto;
import uk.nhs.tis.sync.service.DataRequestService;
import uk.nhs.tis.sync.service.KinesisService;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SyncHandlingJobTest {

  private static final String QUEUE_NAME = "mock-queue-name";

  private static final String QUEUE_URL = "mock-queue-url";

  private SyncHandlingJob job;

  private SyncHandlingJob jobSpy;

  @Mock
  KinesisService kinesisServiceMock;

  @Mock
  DataRequestService dataRequestServiceMock;

  @Mock
  AmazonSQS amazonSqsMock;

  @BeforeEach
  void setUp() {
    GetQueueUrlResult urlResult = new GetQueueUrlResult().withQueueUrl(QUEUE_URL);
    when(amazonSqsMock.getQueueUrl(QUEUE_NAME)).thenReturn(urlResult);

    job = new SyncHandlingJob(kinesisServiceMock,
        dataRequestServiceMock,
        new ObjectMapper(),
        amazonSqsMock,
        QUEUE_NAME);

    jobSpy = spy(job);
  }

  @Test
  void shouldSendDataToKinesis() {
    Message message = new Message();
    message.setBody("{" +
        "\"table\": \"Post\"," +
        "\"id\": \"10\"" +
        "}"
    );
    PostDTO dto = new PostDTO();

    ReceiveMessageResult receiveMessageResult = new ReceiveMessageResult().withMessages(message);
    when(amazonSqsMock.receiveMessage(QUEUE_URL)).thenReturn(receiveMessageResult);
    when(dataRequestServiceMock.retrieveDto(any(AmazonSqsMessageDto.class))).thenReturn(dto);

    job.run();

    verify(kinesisServiceMock).sendData(dto);
  }

  @Test
  void runSyncHandlingJobShouldBeCalled() {
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

    job = new SyncHandlingJob(kinesisServiceMock,
        dataRequestServiceMock,
        objectMapperMock,
        amazonSqsMock,
        QUEUE_NAME);

    job.run();

    Throwable throwable = catchThrowable(() -> job.run());
    assertThat(throwable).isNull();
  }
}
