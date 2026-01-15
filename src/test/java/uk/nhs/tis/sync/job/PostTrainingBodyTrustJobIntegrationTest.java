package uk.nhs.tis.sync.job;

import static org.hamcrest.MatcherAssert.assertThat;

import com.transformuk.hee.tis.tcs.service.repository.PostTrustRepository;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest
@Sql(scripts = {"/scripts/posts.sql"})
@Sql(scripts = {"/scripts/deletePosts.sql"}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
public class PostTrainingBodyTrustJobIntegrationTest {

  @Autowired
  PostTrainingBodyTrustJob job;

  @Autowired
  private PostTrustRepository repo;

  @BeforeEach
  public void setUp() throws Exception {
    repo.deleteAllInBatch();
    assertThat("should have prepared the empty synchronized database table", repo.findAll().size(), CoreMatchers.is(0));
  }

  @AfterEach
  public void tearDown() throws Exception {
    repo.deleteAllInBatch();
  }

  @Test
  public void testJobRun() throws Exception {
    job.postTrainingBodyTrustFullSync();
    int maxLoops = 1440, loops = 0;
    //Loop while the job is running up to 2 hours
    Thread.sleep(5 * 1000L);
    while (job.isCurrentlyRunning() && loops <= maxLoops) {
      System.out.println("Job running");
      Thread.sleep(5 * 1000L);
      loops++;
    }
    assertThat("should the sync job is not currently running", job.isCurrentlyRunning(), CoreMatchers.not(true));
    assertThat("The sync job should not have timed out", loops > maxLoops, CoreMatchers.not(true));
    int size = repo.findAll().size();
    assertThat("should have data in the synchronized database table", size, CoreMatchers.not(0));
  }

}
