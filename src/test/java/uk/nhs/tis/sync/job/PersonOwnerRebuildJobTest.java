package uk.nhs.tis.sync.job;

import static org.mockito.Mockito.verify;

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
@SpringBootTest
public class PersonOwnerRebuildJobTest {

  // Mock the sync handler as it requires an SQS queue to be accessible.
  @MockBean
  private SyncHandlingJob syncHandlingJob;

  @Autowired
  private PersonOwnerRebuildJob job;

  @MockBean
  private PersonRepository repo;

  @Test
  public void testPersonOwnerRebuildJob() throws Exception {
    job.personOwnerRebuildJob();
    Thread.sleep(1000L);
    verify(repo).buildPersonView();
    Assert.assertThat("should the sync job is not currently running", job.isCurrentlyRunning(),
        CoreMatchers.not(true));
  }

}
