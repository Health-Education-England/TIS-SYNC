package uk.nhs.tis.sync.event.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.hubspot.algebra.Result;
import com.hubspot.slack.client.SlackClient;
import com.hubspot.slack.client.models.response.SlackError;
import com.hubspot.slack.client.models.response.chat.ChatPostMessageResponse;
import java.util.concurrent.CompletableFuture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.tis.sync.event.JobExecutionEvent;

@RunWith(MockitoJUnitRunner.class)
public class SlackMessagingEventListenerTest {

  @Mock
  SlackClient mockClient;

  @Mock
  CompletableFuture<Result<ChatPostMessageResponse, SlackError>> mockFuture;

  @Test
  public void testSendJobStatus() {

    // Given we have a slack API client that will respond to any message post
    when(mockClient.postMessage(any())).thenReturn(mockFuture);
    SlackMessagingEventListener testClass = new SlackMessagingEventListener(mockClient, "channel");
    // When a job completion event is received
    JobExecutionEvent event = new JobExecutionEvent(this, "Test message");
    testClass.handleJobExecutionEvent(event);

    // Then our client should receive a message.
    verify(mockClient).postMessage(any());
  }

  @Test
  public void testNoSendWhenChannelIsNull() {

    // Given we have a slack API client that will respond to any message post
    SlackMessagingEventListener testClass = new SlackMessagingEventListener(mockClient, null);
    // When a job completion event is received
    JobExecutionEvent event = new JobExecutionEvent(this, "Test message");
    testClass.handleJobExecutionEvent(event);

    // Then our client should not receive a message.
    verifyNoInteractions(mockClient);
  }

  @Test
  public void testNoSendWhenChannelIsBlank() {

    // Given we have a slack API client that will respond to any message post
    SlackMessagingEventListener testClass = new SlackMessagingEventListener(mockClient, " ");
    // When a job completion event is received
    JobExecutionEvent event = new JobExecutionEvent(this, "Test message");
    testClass.handleJobExecutionEvent(event);

    // Then our client should not receive a message.
    verifyNoInteractions(mockClient);
  }
}
