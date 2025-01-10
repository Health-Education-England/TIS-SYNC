package uk.nhs.tis.sync.message.listener;

import static uk.nhs.tis.sync.service.DataRequestService.TABLE_GMC;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.model.GmcDetailsProvidedEvent;

/**
 * Receive messages from TSS GMC 'update rejected' queue and forward to data request queue to allow
 * TSS to be resync'd to the existing TCS value.
 */
@Profile("!nimdta")
@Component
@Slf4j
public class RabbitMqTssRejectedGmcUpdateListener {
  protected static final String TIS_TRIGGER_MESSAGE = "Update rejected";

  private AmazonSQS sqs;
  private String queueUrl;
  private ObjectMapper objectMapper;

  /**
   * Instantiate the Rabbit TSS rejected GMC update listener.
   *
   * @param sqs          The Amazon SQS object to use.
   * @param queueUrl     The SQS queue to which to post data request messages.
   * @param objectMapper The object mapper to use.
   */
  public RabbitMqTssRejectedGmcUpdateListener(AmazonSQS sqs,
      @Value("${application.aws.sqs.queueUrl}") String queueUrl,
      ObjectMapper objectMapper) {
    this.sqs = sqs;
    this.queueUrl = queueUrl;
    this.objectMapper = objectMapper;
  }

  /**
   * Receive messages from the TSS GMC rejected queue.
   *
   * @param event contains message payload.
   */
  @RabbitListener(queues = "${application.rabbit.trainee.gmc.rejected}")
  public void listenToRejectedGmcUpdates(GmcDetailsProvidedEvent event) {
    try {
      Map<String, String> messageMap = new HashMap<>();
      messageMap.put("id", event.getPersonId().toString());
      messageMap.put("table", TABLE_GMC);
      messageMap.put("tisTrigger", TIS_TRIGGER_MESSAGE);
      messageMap.put("tisTriggerDetail", "Received " + LocalDateTime.now());
      String messageBody = objectMapper.writeValueAsString(messageMap);
      SendMessageRequest msgRequest = new SendMessageRequest(queueUrl, messageBody);
      String messageGroupAndDedup = TABLE_GMC + "_" + event.getPersonId().toString();
      msgRequest.setMessageGroupId(messageGroupAndDedup);
      msgRequest.setMessageDeduplicationId(messageGroupAndDedup);
      sqs.sendMessage(msgRequest);
      log.info("Rejected GMC update received and data request made to reset value in TSS: {}",
          messageBody);
    } catch (JsonProcessingException e) {
      // Do not requeue the message if the event arguments are not valid.
      throw new AmqpRejectAndDontRequeueException(e);
    }
  }
}
