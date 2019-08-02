package uk.nhs.tis.sync;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.tis.sync.job.person.PersonElasticSearchSyncJob;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JobRunningListenerTest {

  /**
   * Mock of the elastic search job, as there isn't a suitable elastic search test component
   */
  @MockBean
  PersonElasticSearchSyncJob personElasticSearchSyncJob;

  @Autowired
  JobRunningListener testClass;

  @Test
  public void testRunJobs() {
    when(personElasticSearchSyncJob.isCurrentlyRunning()).thenReturn(false);
    testClass.runJobs();
    verify(personElasticSearchSyncJob).personElasticSearchSync();
    verify(personElasticSearchSyncJob).isCurrentlyRunning();
  }

}
