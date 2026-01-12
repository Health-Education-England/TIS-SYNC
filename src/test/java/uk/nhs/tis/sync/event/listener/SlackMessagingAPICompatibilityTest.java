package uk.nhs.tis.sync.event.listener;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.hubspot.slack.client.SlackClient;
import com.hubspot.slack.client.SlackClientFactory;
import com.hubspot.slack.client.SlackClientRuntimeConfig;
import com.hubspot.slack.client.methods.params.chat.ChatPostMessageParams;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;


@MockitoSettings(strictness = Strictness.WARN)
@ExtendWith(MockitoExtension.class)
public class SlackMessagingAPICompatibilityTest {

  // we have to scrape the stream output to see what is actually POST'ed to the Slack API
  private final ByteArrayOutputStream out = new ByteArrayOutputStream();
  private final PrintStream originalOut = System.out;

  @BeforeEach
  public void setStream() {
    System.setOut(new PrintStream(out));
  }

  @AfterEach
  public void restoreInitialStream() {
    System.setOut(originalOut);
  }

  @Test
  public void testSlackPostAPICompatibility() {

    String SLACK_TOKEN_NEW_APP = "ANY_TOKEN";
    SlackClientRuntimeConfig slackClientRuntimeConfig = SlackClientRuntimeConfig.builder()
        .setTokenSupplier(() -> SLACK_TOKEN_NEW_APP).build();
    SlackClient slackClient = SlackClientFactory.defaultFactory().build(slackClientRuntimeConfig);

    ChatPostMessageParams params = ChatPostMessageParams.builder()
        .setChannelId("test")
        .setText("test")
        .build();

    slackClient.postMessage(params).join();

    Boolean asUserParameterExists = out.toString().contains("as_user");

    assertFalse(asUserParameterExists);
  }
}
