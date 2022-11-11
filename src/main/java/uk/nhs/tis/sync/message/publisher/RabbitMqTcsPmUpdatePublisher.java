package uk.nhs.tis.sync.message.publisher;

import java.util.Set;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * publish messages to rabbitMq queues.
 */
@Profile("!nimdta")
@Component
public class RabbitMqTcsPmUpdatePublisher {

  @Value("${application.rabbit.reval.exchange}")
  private String exchange;

  @Value("${application.rabbit.reval.routingKey.currentpm.update}")
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
