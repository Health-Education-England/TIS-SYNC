package uk.nhs.tis.sync.message.publisher;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Sets;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RabbitmqTcsPmUpdatePublisherTest {

  @InjectMocks
  RabbitMqTcsRevalTraineeUpdatePublisher rabbitMqTcsRevalTraineeUpdatePublisher;

  @Mock
  RabbitTemplate rabbitTemplate;

  @Captor
  ArgumentCaptor<String> exchangeNameCaptor;

  @Captor
  ArgumentCaptor<String> routingKeyNameCaptor;

  @Captor
  ArgumentCaptor<Set<String>> messageCaptor;

  @Test
  void shouldPublishToRabbitMq() {
    String routingKeyName = "routingKeyName";
    String exchangeName = "exchangeName";

    ReflectionTestUtils.setField(rabbitMqTcsRevalTraineeUpdatePublisher, "routingKey", routingKeyName);
    ReflectionTestUtils.setField(rabbitMqTcsRevalTraineeUpdatePublisher, "exchange", exchangeName);

    rabbitMqTcsRevalTraineeUpdatePublisher.publishToBroker(Sets.newHashSet("11111", "22222"));

    verify(rabbitTemplate).convertAndSend(
        exchangeNameCaptor.capture(),
        routingKeyNameCaptor.capture(),
        messageCaptor.capture()
    );

    Set<String> messagesPublished = messageCaptor.getValue();
    assertThat(messagesPublished, hasItems("11111", "22222"));
    assertThat(routingKeyNameCaptor.getValue(), is(routingKeyName));
    assertThat(exchangeNameCaptor.getValue(), is(exchangeName));
  }
}
