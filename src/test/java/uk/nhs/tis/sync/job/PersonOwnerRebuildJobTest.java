package uk.nhs.tis.sync.job;

import static org.mockito.Mockito.verify;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import com.transformuk.hee.tis.tcs.service.repository.PersonRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PersonOwnerRebuildJobTest {

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
