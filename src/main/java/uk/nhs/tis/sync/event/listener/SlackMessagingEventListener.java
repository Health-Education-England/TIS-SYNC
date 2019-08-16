package uk.nhs.tis.sync.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.hubspot.slack.client.SlackClient;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import uk.nhs.tis.sync.event.JobExecutionEvent;

@Component
public class SlackMessagingEventListener {

  private static final Logger LOG = LoggerFactory.getLogger(SlackMessagingEventListener.class);

  @Value("${slack.job.notification-channel}")
  private String CHANNEL_ID = "";

  SlackClient slackClient;

  @Autowired
  public SlackMessagingEventListener(SlackClient slackClient) {
    this.slackClient = slackClient;
  }

  @EventListener
  public void handleJobCompletionEvent(JobExecutionEvent event) {
    LOG.debug("Received job completion event with message [" + event.getMessage() + "]");
    ChatPostMessageParams params = ChatPostMessageParams.builder().setChannelId(CHANNEL_ID)
        .setText(event.getMessage()).build();
    slackClient.postMessage(params);
  }

}
