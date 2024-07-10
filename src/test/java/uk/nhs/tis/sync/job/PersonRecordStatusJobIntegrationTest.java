package uk.nhs.tis.sync.job;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import java.util.concurrent.TimeUnit;
import org.awaitility.core.ConditionTimeoutException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest//?Need this?(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
//TODO Write scripts so we can see some records are being updated.@Sql(scripts = {"/scripts/programmes.sql","/scripts/personRows.sql","/scripts/programmeMemberships.sql"})
//TODO @Sql(scripts = {"/scripts/deleteProgrammeMemberships.sql","/scripts/deletePersonRows.sql","/scripts/deleteProgrammes.sql"}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class PersonRecordStatusJobIntegrationTest {

  @MockBean
  ProfileServiceImpl profileService;

  @Autowired
  PersonRecordStatusJob job;

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
  }

  @DisplayName("run personRecordStatusJob with correct date argument")
  @ParameterizedTest(name = "Should return run job when it is triggered with \"{0}\".")
  @ValueSource(strings = {
      "ANY",
      "none",
      "2022-01-01",
      ""
  })
  void testJobRunWithCorrectArg(String arg) {
    job.run(String.format("{\"dateOverride\":\"%s\"}", arg));

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
  }

  @DisplayName("run personRecordStatusJob with incorrect date argument")
  @ParameterizedTest(name = "Should throw exception when personRecordStatusJob is triggered with \"{0}\".")
  @ValueSource(strings = {
      "aaa",
      "01/01/2020",
      "2022-02-30",
  })
  void testJobShouldThrowExceptionWithIncorrectArg(String arg) {
    String dateParam = String.format("{\"dateOverride\":\"%s\"}", arg);
    Exception exception = assertThrows(IllegalArgumentException.class,
        () -> job.run(dateParam));
    String errMsg = exception.getMessage();
    assertThat("should the sync job is not currently running",
        job.isCurrentlyRunning(), CoreMatchers.is(false));
    assertThat("should not be currently running",
        errMsg, CoreMatchers.containsString("The date is not correct"));
  }
}
