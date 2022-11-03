package uk.nhs.tis.sync.message.publisher;

import java.util.Set;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * publish messages to rabbitMq queues.
 */
@Component
public class RabbitMqTcsPmUpdatePublisher {

  @Value("${application.rabbit.reval.exchange}")
  private String exchange;

  @Value("${application.rabbit.reval.routingKey.currentpm.nightlysync}")
  private String routingKey;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  /**
   * Publishes message to injected broker interface.
   *
   * @param message contains message payload
   */
  public void publishToBroker(Set<String> message) {
    rabbitTemplate.convertAndSend(exchange, routingKey, message);
  }
}
