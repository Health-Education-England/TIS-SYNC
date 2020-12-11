package uk.nhs.tis.sync.event.listener;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import com.hubspot.slack.client.SlackClientFactory;
import com.hubspot.slack.client.SlackClientRuntimeConfig;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import com.hubspot.slack.client.SlackClient;
import static org.junit.Assert.assertEquals;


@RunWith(MockitoJUnitRunner.class)
public class SlackMessagingAPICompatibilityTest  {

  // we have to scrape the stream output to see what is actually POST'ed to the Slack API
  private final ByteArrayOutputStream out = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @Before
  public void setStream() {
    System.setOut(new PrintStream(out));
  }

  @After
  public void restoreInitialStream() {
    System.setOut(originalOut);
  }

  @Test
  public void testSlackPostAPICompatibility() {

    String SLACK_TOKEN_NEW_APP = "ANY_TOKEN";
    SlackClientRuntimeConfig slackClientRuntimeConfig = SlackClientRuntimeConfig.builder().setTokenSupplier(() -> SLACK_TOKEN_NEW_APP).build();
    SlackClient slackClient = SlackClientFactory.defaultFactory().build(slackClientRuntimeConfig);

    ChatPostMessageParams params = ChatPostMessageParams.builder()
      .setChannelId("test")
      .setText("test")
      .build();

    slackClient.postMessage(params).join();

    Boolean noAsUserParameter = out.toString().indexOf("as_user") == -1;

    assertEquals(noAsUserParameter, true);
  }
}
