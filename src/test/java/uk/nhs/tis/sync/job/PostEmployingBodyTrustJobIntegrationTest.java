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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Sql(scripts = {"/scripts/posts.sql"})
@Sql(scripts = {"/scripts/deletePosts.sql"}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
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
    repo.deleteAllInBatch();
  }

  @Test
  public void testJobRun() throws Exception {
    job.PostEmployingBodyTrustFullSync();
    int maxLoops = 1440, loops = 0;
    //Loop while the job is running up to 2 hours
    Thread.sleep(5 * 1000L);
    while (job.isCurrentlyRunning() && loops <= maxLoops) {
      System.out.println("Job running");
      Thread.sleep(5 * 1000L);
      loops++;
    }
    Assert.assertThat("should the sync job is not currently running", job.isCurrentlyRunning(), CoreMatchers.not(true));
    Assert.assertThat("The sync job should not have timed out", loops > maxLoops, CoreMatchers.not(true));
    int size = repo.findAll().size();
    Assert.assertThat("should have data in the synchronized database table", size, CoreMatchers.not(0));
  }

}
