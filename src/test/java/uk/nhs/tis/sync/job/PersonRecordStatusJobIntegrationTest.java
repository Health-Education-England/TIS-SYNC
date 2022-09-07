package uk.nhs.tis.sync.job;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.transformuk.hee.tis.tcs.service.repository.PersonRepository;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest//?Need this?(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
//TODO Write scripts so we can see some records are being updated.@Sql(scripts = {"/scripts/programmes.sql","/scripts/personRows.sql","/scripts/programmeMemberships.sql"})
//TODO @Sql(scripts = {"/scripts/deleteProgrammeMemberships.sql","/scripts/deletePersonRows.sql","/scripts/deleteProgrammes.sql"}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class PersonRecordStatusJobIntegrationTest {

  @Autowired
  PersonRecordStatusJob job;

  @Autowired
  private PersonRepository repo;

  @Test
  public void testJobRun() throws Exception {
    job.personRecordStatusJob(null);
    int maxLoops = 1440, loops = 0;
    //Loop while the job is running up to 2 hours
    Thread.sleep(1 * 1000L);
    while (job.isCurrentlyRunning() && loops <= maxLoops) {
      System.out.println("Job running");
      Thread.sleep(1 * 1000L);
      loops++;
    }
    assertThat("should the sync job is not currently running", job.isCurrentlyRunning(), CoreMatchers.not(true));
    assertThat("then the sync job should not have timed out", loops > maxLoops, CoreMatchers.not(true));
  }

  @DisplayName("run personRecordStatusJob with correct date argument")
  @ParameterizedTest(name = "Should return run job when it is triggered with \"{0}\".")
  @ValueSource(strings = {
      "ANY",
      "AWS",
      "2022-01-01",
      ""
  })
  public void testJobRunWithCorrectArg(String arg) throws Exception {
    job.personRecordStatusJob(arg);
    int maxLoops = 1440, loops = 0;
    //Loop while the job is running up to 2 hours
    Thread.sleep(1 * 1000L);
    while (job.isCurrentlyRunning() && loops <= maxLoops) {
      System.out.println("Job running");
      Thread.sleep(1 * 1000L);
      loops++;
    }
    assertThat("should the sync job is not currently running", job.isCurrentlyRunning(), CoreMatchers.not(true));
    assertThat("then the sync job should not have timed out", loops > maxLoops, CoreMatchers.not(true));
  }

  @DisplayName("run personRecordStatusJob with incorrect date argument")
  @ParameterizedTest(name = "Should throw exception when personRecordStatusJob is triggered with \"{0}\".")
  @ValueSource(strings = {
      "aaa",
      "01/01/2020",
      "2022-02-30",
  })
  public void testJobShouldThrowExceptionWithIncorrectArg(String arg) {
    Exception exception = assertThrows(IllegalArgumentException.class, () -> job.personRecordStatusJob(arg));
    String errMsg = exception.getMessage();
    assertThat("should the sync job is not currently running",
        job.isCurrentlyRunning(), CoreMatchers.not(true));
    assertThat("should throw exception for the incorrect date argument",
        errMsg, CoreMatchers.containsString("The date is not correct"));
  }
}
