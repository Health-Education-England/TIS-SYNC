package uk.nhs.tis.sync.job;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.tis.sync.dto.AmazonSqsMessageDto;
import uk.nhs.tis.sync.service.DataRequestService;
import uk.nhs.tis.sync.service.SendDataIntoKinesisService;

import static com.google.common.collect.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SyncHandlingJobTest {

  @Autowired
  private SyncHandlingJob job;

  @MockBean
  SendDataIntoKinesisService sendDataIntoKinesisServiceMock;

  @MockBean
  DataRequestService dataRequestServiceMock;

  @MockBean
  AmazonSQS amazonSQSMock;

  String QUEUE_NAME = "mock-queue-name";

  String QUEUE_URL = "mock-queue-url";

  @BeforeEach
  public void setUp() {
    job = new SyncHandlingJob(sendDataIntoKinesisServiceMock,
        dataRequestServiceMock,
        new ObjectMapper(),
        amazonSQSMock,
        QUEUE_NAME);
  }

  @Test
  public void runTest() {
    Message message = new Message();
    message.setBody("{" +
      "\"table\": \"Post\"," +
      "\"id\": \"10\"" +
      "}"
    );
    Object dto = new PostDTO();
    ReceiveMessageResult receiveMessageResult = mock(ReceiveMessageResult.class);
    when(amazonSQSMock.receiveMessage(anyString())).thenReturn(receiveMessageResult);
    when(receiveMessageResult.getMessages()).thenReturn(newArrayList(message));
    when(dataRequestServiceMock.retrieveDto(any(AmazonSqsMessageDto.class))).thenReturn(dto);

    GetQueueUrlResult getQueueUrlResultMock = mock(GetQueueUrlResult.class);

    when(amazonSQSMock.getQueueUrl(anyString())).thenReturn(getQueueUrlResultMock);
    when(getQueueUrlResultMock.getQueueUrl()).thenReturn(QUEUE_URL);

    job.syncHandlingJob();

    try {
      Thread.sleep(5 * 1000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    verify(sendDataIntoKinesisServiceMock).sendDataIntoKinesisStream(any(Object.class), eq("Post"));
  }
}
