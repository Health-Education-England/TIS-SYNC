package uk.nhs.tis.sync.message.publisher;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Lists;
import java.util.List;
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
public class RabbitmqTcsPmUpdatePublisherTest {

  @InjectMocks
  RabbitMqTcsPmUpdatePublisher rabbitMqTcsPmUpdatePublisher;

  @Mock
  RabbitTemplate rabbitTemplate;

  @Captor
  ArgumentCaptor<String> exchangeNameCaptor;

  @Captor
  ArgumentCaptor<String> routingKeyNameCaptor;

  @Captor
  ArgumentCaptor<List<String>> messageCaptor;

  @Test
  void shouldPublishToRabbitMq() {
    String routingKeyName = "routingKeyName";
    String exchangeName = "exchangeName";

    ReflectionTestUtils.setField(
        rabbitMqTcsPmUpdatePublisher, "routingKey", routingKeyName
    );
    ReflectionTestUtils.setField(
        rabbitMqTcsPmUpdatePublisher, "exchange", exchangeName
    );

    rabbitMqTcsPmUpdatePublisher.publishToBroker(Lists.newArrayList("11111", "22222"));

    verify(rabbitTemplate).convertAndSend(
        exchangeNameCaptor.capture(),
        routingKeyNameCaptor.capture(),
        messageCaptor.capture()
    );

    List<String> messagesPublished = messageCaptor.getValue();
    assertThat(messagesPublished, hasItem("11111"));
    assertThat(messagesPublished, hasItem("22222"));
    assertThat(routingKeyNameCaptor.getValue(), is(routingKeyName));
    assertThat(exchangeNameCaptor.getValue(), is(exchangeName));
  }
}
