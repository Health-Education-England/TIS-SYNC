package uk.nhs.tis.sync.job.reval;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.awaitility.core.ConditionTimeoutException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.tis.sync.message.publisher.RabbitMqTcsPmUpdatePublisher;

@RunWith(SpringRunner.class)
@SpringBootTest
@Sql(scripts = {"/scripts/personRows.sql", "/scripts/programmes.sql",
    "/scripts/programmeMemberships.sql"})
@Sql(scripts = {"/scripts/deleteProgrammeMemberships.sql", "/scripts/deleteProgrammes.sql",
    "/scripts/deletePersonRows.sql"}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class RevalCurrentPmSyncJobIntegrationTest {

  @Autowired
  RevalCurrentPmSyncJob job;

  @SpyBean
  RabbitMqTcsPmUpdatePublisher rabbitMqPublisher;

  @Captor
  ArgumentCaptor<Set<String>> messageCaptor;

  @Test
  void testJobRun() {

    job.run(null);

    try {
      await().atLeast(1, TimeUnit.SECONDS)
          .atMost(2, TimeUnit.HOURS)
          .with()
          .pollInterval(1, TimeUnit.SECONDS)
          .until(() -> !job.isCurrentlyRunning());
      assertThat("should not be currently running",
          job.isCurrentlyRunning(), CoreMatchers.is(false));
    } catch (ConditionTimeoutException e) {
      Assert.fail("the sync job should not have timed out");
    }

    verify(rabbitMqPublisher).publishToBroker(messageCaptor.capture());
    Set<String> messages = messageCaptor.getValue();
    assertThat("should send message for all person whose current PM changes nightly",
        messages.size(), CoreMatchers.is(2));
  }
}
