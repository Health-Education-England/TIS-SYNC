package uk.nhs.tis.sync.job;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.tis.sync.repository.PersonElasticSearchRepository;
import uk.nhs.tis.sync.job.person.PersonElasticSearchSyncJob;

@RunWith(SpringRunner.class)
@SpringBootTest
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
  public void testJobRun() throws Exception{
    job.personElasticSearchSync();
    int timeout = 120;
    // every minute within timeout's time, check if the job has been done
    for (int i = 0; i < timeout; i++) {
      Thread.sleep(1 * 60 * 1000L);
      if (!job.isCurrentlyRunning()) {
        break;
      }
    }
    Assert.assertThat("should the sync job is not currently running", job.isCurrentlyRunning(), CoreMatchers.not(true));
    long size = repo.count();
    Assert.assertThat("should have created index in elasticSearch", elasticSearchOperations.indexExists(ES_INDEX), CoreMatchers.is(true));
    Assert.assertThat("should have synchronized data to elasticSearch", size, CoreMatchers.not(0));
  }

}
