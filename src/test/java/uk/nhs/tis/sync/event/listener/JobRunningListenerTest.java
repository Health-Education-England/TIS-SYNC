package uk.nhs.tis.sync.event.listener;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.tis.sync.job.PersonOwnerRebuildJob;
import uk.nhs.tis.sync.job.RecordResendingJob;
import uk.nhs.tis.sync.job.person.PersonElasticSearchSyncJob;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JobRunningListenerTest {

  /**
   * Mock of the following jobs as there aren't suitable test fixtures
   */
  @MockBean
  private PersonElasticSearchSyncJob personElasticSearchSyncJob;
  @MockBean
  private PersonOwnerRebuildJob personOwnerRebuildJob;
//  @MockBean
//  private RecordResendingJob recordResendingJob;

  @Autowired
  JobRunningListener testClass;

  @Test
  public void testRunJobs() {
    when(personOwnerRebuildJob.isCurrentlyRunning()).thenReturn(false);
    when(personElasticSearchSyncJob.isCurrentlyRunning()).thenReturn(false);
    testClass.runJobs();
    verify(personOwnerRebuildJob).personOwnerRebuildJob();
    verify(personOwnerRebuildJob).isCurrentlyRunning();
    verify(personElasticSearchSyncJob).personElasticSearchSync();
    verify(personElasticSearchSyncJob).isCurrentlyRunning();
  }
}
