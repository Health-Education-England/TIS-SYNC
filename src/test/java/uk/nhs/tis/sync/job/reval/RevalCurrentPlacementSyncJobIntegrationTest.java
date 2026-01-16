package uk.nhs.tis.sync.job.reval;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.TimeUnit;
import org.awaitility.core.ConditionTimeoutException;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import uk.nhs.tis.sync.message.publisher.RabbitMqTcsRevalTraineeUpdatePublisher;

@SpringBootTest
class RevalCurrentPlacementSyncJobIntegrationTest {

  @Autowired
  RevalCurrentPlacementSyncJob job;

  @SpyBean
  RabbitMqTcsRevalTraineeUpdatePublisher rabbitMqPublisher;

  @Test
  void testJobRun() {

    job.run(null);

    try {
      await().atLeast(1, TimeUnit.SECONDS)
          .atMost(2, TimeUnit.HOURS)
          .with()
          .pollInterval(1, TimeUnit.SECONDS)
          .until(() -> !job.isCurrentlyRunning());
      assertThat("Job should not be currently running",
          job.isCurrentlyRunning(), CoreMatchers.is(false));
    } catch (ConditionTimeoutException e) {
      Assertions.fail("the sync job should not have timed out");
    }
  }
}
