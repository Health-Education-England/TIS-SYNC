package uk.nhs.tis.sync.job;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.tis.sync.dto.AmazonSqsMessageDto;
import uk.nhs.tis.sync.service.DataRequestService;
import uk.nhs.tis.sync.service.SendDataIntoKinesisStreamService;

@ExtendWith(MockitoExtension.class)
class SyncHandlingJobTest {

  private static final String QUEUE_NAME = "mock-queue-name";

  private static final String QUEUE_URL = "mock-queue-url";

  private SyncHandlingJob job;

  @Mock
  SendDataIntoKinesisStreamService sendDataIntoKinesisStreamServiceMock;

  @Mock
  DataRequestService dataRequestServiceMock;

  @Mock
  AmazonSQS amazonSqsMock;


  @BeforeEach
  void setUp() {
    GetQueueUrlResult urlResult = new GetQueueUrlResult().withQueueUrl(QUEUE_URL);
    when(amazonSqsMock.getQueueUrl(QUEUE_NAME)).thenReturn(urlResult);

    job = new SyncHandlingJob(sendDataIntoKinesisStreamServiceMock,
        dataRequestServiceMock,
        new ObjectMapper(),
        amazonSqsMock,
        QUEUE_NAME);
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

    //verify(sendDataIntoKinesisServiceMock).sendDataIntoKinesisStream(dto);
  }
}
