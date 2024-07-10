package uk.nhs.tis.sync.job.reval;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.verify;

import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.tis.sync.message.publisher.RabbitMqTcsRevalTraineeUpdatePublisher;

@RunWith(SpringRunner.class)
@SpringBootTest
class RevalCurrentPlacementSyncJobIntegrationTest {

  @MockBean
  ProfileServiceImpl profileService;

  @Autowired
  RevalCurrentPlacementSyncJob job;

  @SpyBean
  RabbitMqTcsRevalTraineeUpdatePublisher rabbitMqPublisher;

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
      assertThat("Job should not be currently running",
          job.isCurrentlyRunning(), CoreMatchers.is(false));
    } catch (ConditionTimeoutException e) {
      Assert.fail("the sync job should not have timed out");
    }
  }
}
