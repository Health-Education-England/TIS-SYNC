package uk.nhs.tis.sync.message.listener;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.nhs.tis.sync.message.listener.RabbitMqTssRejectedGmcUpdateListener.TIS_TRIGGER_MESSAGE;
import static uk.nhs.tis.sync.service.DataRequestService.TABLE_GMC;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import uk.nhs.tis.sync.model.GmcDetailsProvidedEvent;

@ExtendWith(MockitoExtension.class)
class RabbitMqTssRejectedGmcUpdateListenerTest {

  private static final String DATA_REQUEST_QUEUE_URL = "queue";
  private static final Long PERSON_ID = 123L;
  private static final String GMC_NO = "1234567";
  private static final String GMC_STATUS = "status";

  private SqsClient sqs;
  private ObjectMapper objectMapper;
  private RabbitMqTssRejectedGmcUpdateListener listener;

  private GmcDetailsProvidedEvent event;

  @BeforeEach
  void setUp() {
    sqs = mock(SqsClient.class);
    objectMapper = Mockito.spy(new ObjectMapper().findAndRegisterModules());
    listener = new RabbitMqTssRejectedGmcUpdateListener(sqs, DATA_REQUEST_QUEUE_URL, objectMapper);
    GmcDetailsDTO gmcDetailsDto = new GmcDetailsDTO();
    gmcDetailsDto.setGmcNumber(GMC_NO);
    gmcDetailsDto.setGmcStatus(GMC_STATUS);
    gmcDetailsDto.setId(PERSON_ID);
    gmcDetailsDto.setGmcStartDate(LocalDate.MIN);
    gmcDetailsDto.setGmcEndDate(LocalDate.MAX);
    gmcDetailsDto.setAmendedDate(LocalDateTime.now());
    event = new GmcDetailsProvidedEvent(PERSON_ID, gmcDetailsDto);
  }

  @Test
  void shouldListenToRejectedGmcMessagesAndRepostToDataRequestSqs() throws JsonProcessingException {
    ArgumentCaptor<SendMessageRequest> msgRequestCaptor
        = ArgumentCaptor.forClass(SendMessageRequest.class);

    listener.listenToRejectedGmcUpdates(event);

    verify(sqs).sendMessage(msgRequestCaptor.capture());
    verifyNoMoreInteractions(sqs);

    SendMessageRequest sendMessageRequest = msgRequestCaptor.getValue();
    assertThat("Unexpected message URL.", sendMessageRequest.queueUrl(),
        is(DATA_REQUEST_QUEUE_URL));
    String expectedMsgGroupIdAndDedup = TABLE_GMC + "_" + event.getPersonId().toString();
    assertThat("Unexpected message group Id.", sendMessageRequest.messageGroupId(),
        is(expectedMsgGroupIdAndDedup));
    assertThat("Unexpected deduplication Id.", sendMessageRequest.messageDeduplicationId(),
        is(expectedMsgGroupIdAndDedup));

    String messageBody = sendMessageRequest.messageBody();
    Map<String, String> sentMessageMap = objectMapper.readValue(messageBody, Map.class);

    assertThat("Unexpected message body id.", sentMessageMap.get("id"),
        is(PERSON_ID.toString()));
    assertThat("Unexpected message body table.", sentMessageMap.get("table"),
        is(TABLE_GMC));
    assertThat("Unexpected message body tisTrigger.", sentMessageMap.get("tisTrigger"),
        is(TIS_TRIGGER_MESSAGE));
    assertThat("Unexpected message body tisTriggerDetail.",
        sentMessageMap.get("tisTriggerDetail").startsWith("Received "), is(true));
  }

  @Test
  void shouldNotRequeueFaultyMessages() throws JsonProcessingException {
    doThrow(new JsonProcessingException("") {}).when(objectMapper).writeValueAsString(any());

    assertThrows(AmqpRejectAndDontRequeueException.class,
        () -> listener.listenToRejectedGmcUpdates(event));
    verifyNoInteractions(sqs);
  }
}
