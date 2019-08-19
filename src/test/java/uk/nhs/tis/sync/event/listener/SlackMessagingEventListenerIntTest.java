package uk.nhs.tis.sync.event.listener;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import uk.nhs.tis.sync.event.JobExecutionEvent;

/**
 * Use this test to send messages to slack.
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Ignore
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
