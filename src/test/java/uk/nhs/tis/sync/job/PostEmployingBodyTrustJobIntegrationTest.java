package uk.nhs.tis.sync.job;

import com.transformuk.hee.tis.tcs.service.repository.PostTrustRepository;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PostEmployingBodyTrustJobIntegrationTest {
  
  @Autowired
  PostEmployingBodyTrustJob job;

  @Autowired
  private PostTrustRepository repo;

  @Before
  public void setUp() throws Exception {
    job.deleteData();
    Assert.assertThat("should have prepared the empty synchronized database table", repo.findAll().size(), CoreMatchers.is(0));
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testJobRun() throws Exception{
    job.PostEmployingBodyTrustFullSync();
    // every minute within 15 mins, check if the job has been done
    for (int i = 0; i < 15; i++) {
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