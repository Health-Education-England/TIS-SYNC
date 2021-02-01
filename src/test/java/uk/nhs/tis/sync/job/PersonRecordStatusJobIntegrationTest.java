package uk.nhs.tis.sync.job;

import com.transformuk.hee.tis.tcs.service.repository.PersonRepository;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest//?Need this?(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
//TODO Write scripts so we can see some records are being updated.@Sql(scripts = {"/scripts/programmes.sql","/scripts/personRows.sql","/scripts/programmeMemberships.sql"})
//TODO @Sql(scripts = {"/scripts/deleteProgrammeMemberships.sql","/scripts/deletePersonRows.sql","/scripts/deleteProgrammes.sql"}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class PersonRecordStatusJobIntegrationTest {

  // Mock the sync handler as it requires an SQS queue to be accessible.
  @MockBean
  private SyncHandlingJob syncHandlingJob;

  @Autowired
  PersonRecordStatusJob job;

  @Autowired
  private PersonRepository repo;

  @Test
  public void testJobRun() throws Exception {
    job.personRecordStatusJob();
    int maxLoops = 1440, loops = 0;
    //Loop while the job is running up to 2 hours
    Thread.sleep(1 * 1000L);
    while (job.isCurrentlyRunning() && loops <= maxLoops) {
      System.out.println("Job running");
      Thread.sleep(1 * 1000L);
      loops++;
    }
    Assert.assertThat("should the sync job is not currently running", job.isCurrentlyRunning(), CoreMatchers.not(true));
    Assert.assertThat("then the sync job should not have timed out", loops > maxLoops, CoreMatchers.not(true));
  }

}
