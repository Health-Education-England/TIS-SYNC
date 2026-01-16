package uk.nhs.tis.sync.event.listener;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.nhs.tis.sync.event.JobExecutionEvent;

/**
 * Use this test to send messages to slack.
 *
 */
@SpringBootTest
@Disabled
public class SlackMessagingEventListenerIntTest {

  @Autowired
  SlackMessagingEventListener testClass;

  @Test
  public void testSendJobStatus() {

    // When a job completion event is received
    JobExecutionEvent event = new JobExecutionEvent(this, "Slack integration test run.");
    testClass.handleJobExecutionEvent(event);

  }

}
