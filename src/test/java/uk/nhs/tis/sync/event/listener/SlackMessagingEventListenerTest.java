package uk.nhs.tis.sync.event.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.concurrent.CompletableFuture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import com.hubspot.algebra.Result;
import com.hubspot.slack.client.SlackClient;
import com.hubspot.slack.client.models.response.SlackError;
import com.hubspot.slack.client.models.response.chat.ChatPostMessageResponse;
import uk.nhs.tis.sync.event.JobExecutionEvent;

@RunWith(MockitoJUnitRunner.class)
public class SlackMessagingEventListenerTest {

  @Mock
  SlackClient mockClient;

  @Test
  public void testSendJobStatus() {

    // Given we have a slack API client that will respond to any message post
    when(mockClient.postMessage(any())).thenReturn(buildMockSlackResultAnswer());
    SlackMessagingEventListener testClass = new SlackMessagingEventListener(mockClient);

    // When a job completion event is received
    JobExecutionEvent event = new JobExecutionEvent(this, "Test message");
    testClass.handleJobCompletionEvent(event);

    // Then our client should receive a message.
    verify(mockClient).postMessage(any());
  }

  public static CompletableFuture<Result<ChatPostMessageResponse, SlackError>> buildMockSlackResultAnswer() {
    CompletableFuture<Result<ChatPostMessageResponse, SlackError>> mockResult =
        mock(CompletableFuture.class);
    return mockResult;
  }
}
