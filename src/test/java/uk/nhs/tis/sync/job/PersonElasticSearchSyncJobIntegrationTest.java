package uk.nhs.tis.sync.job;

import com.transformuk.hee.tis.tcs.service.repository.PersonElasticSearchRepository;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.tis.sync.Application;
import uk.nhs.tis.sync.job.person.PersonElasticSearchSyncJob;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Ignore
public class PersonElasticSearchSyncJobIntegrationTest {

  @Autowired
  PersonElasticSearchSyncJob job;

  @Autowired
  private ElasticsearchOperations elasticSearchOperations;

  @Autowired
  private PersonElasticSearchRepository repo;

  private static final String ES_INDEX = "persons";

  @Before
  public void setUp() throws Exception {
    elasticSearchOperations.deleteIndex(ES_INDEX);
    Assert.assertThat("should have deleted the index", elasticSearchOperations.indexExists(ES_INDEX), CoreMatchers.is(false));
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testJobRun() throws Exception {
    job.personElasticSearchSync();
    int maxLoops = 1440, loops = 0;
    //Loop while the job is running up to 2 hours
    Thread.sleep(5 * 1000L);
    while (job.isCurrentlyRunning() && loops <= maxLoops) {
      System.out.println("Job running");
      Thread.sleep(5 * 1000L);
      loops++;
    }
    Assert.assertThat("should the sync job is not currently running", job.isCurrentlyRunning(), CoreMatchers.not(true));
    Assert.assertThat("then the sync job should not have timed out", loops > maxLoops, CoreMatchers.not(true));
    long size = repo.count();
    Assert.assertThat("should have created index in elasticSearch", elasticSearchOperations.indexExists(ES_INDEX), CoreMatchers.is(true));
    Assert.assertThat("should have synchronized data to elasticSearch", size, CoreMatchers.not(0));
  }

}
