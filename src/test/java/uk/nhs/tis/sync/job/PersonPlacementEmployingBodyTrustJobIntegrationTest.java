package uk.nhs.tis.sync.job;

import com.transformuk.hee.tis.tcs.service.repository.PersonTrustRepository;
import uk.nhs.tis.sync.Application;
import org.hamcrest.CoreMatchers;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
//@Sql({"/scripts/posts.sql","/scripts/personRows.sql","/scripts/placements.sql"})
//@Sql(scripts = {"/scripts/deletePlacements.sql","/scripts/deletePersonRows.sql","/scripts/deletePosts.sql"}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
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
    repo.deleteAllInBatch();
  }

  @Test
  public void testJobRun() throws Exception {
    job.doPersonPlacementEmployingBodyFullSync();
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
//    Assert.assertThat("should have data in the synchronized database table", size, CoreMatchers.not(0));
  }

}
