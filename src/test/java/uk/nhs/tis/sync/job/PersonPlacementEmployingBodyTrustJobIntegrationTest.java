package uk.nhs.tis.sync.job;

import com.transformuk.hee.tis.tcs.service.repository.PersonTrustRepository;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PersonPlacementEmployingBodyTrustJobIntegrationTest {
  
  @Autowired
  PersonPlacementEmployingBodyTrustJob job;

  @Autowired
  PersonTrustRepository repo;

  @Before
  public void setUp() throws Exception {
    repo.deleteAllInBatch();
    Assert.assertThat("should have prepared the empty synchronized database table", repo.findAll().size(), CoreMatchers.is(0));
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testJobRun() throws Exception{
    job.doPersonPlacementEmployingBodyFullSync();
    int timeout = 120;
    // every minute within timeout's time, check if the job has been done
    for (int i = 0; i < timeout; i++) {
      Thread.sleep(1 * 60 * 1000L);
      if (!job.isCurrentlyRunning()) {
        break;
      }
    }
    Assert.assertThat("should the sync job is not currently running", job.isCurrentlyRunning(), CoreMatchers.not(true));
    int size = repo.findAll().size();
    Assert.assertThat("should have data in the synchronized database table", size, CoreMatchers.not(0));
  }

}
