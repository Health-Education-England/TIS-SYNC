package uk.nhs.tis.sync.event.listener;

import com.hubspot.slack.client.SlackClient;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.event.JobExecutionEvent;

@Component
public class SlackMessagingEventListener {

  private static final Logger LOG = LoggerFactory.getLogger(SlackMessagingEventListener.class);
  SlackClient slackClient;
  private String channelId;

  public SlackMessagingEventListener(SlackClient slackClient,
      @Value("${slack.job.notification-channel}") String channelId) {
    this.slackClient = slackClient;
    this.channelId = channelId;
  }

  @EventListener
  public void handleJobExecutionEvent(JobExecutionEvent event) {
    LOG.debug("Received job completion event with message [{}]", event.getMessage());
    if (!StringUtils.isBlank(channelId)) {
      ChatPostMessageParams params = ChatPostMessageParams.builder().setChannelId(channelId)
          .setText(event.getMessage()).build();
      slackClient.postMessage(params);
    }
  }

}
