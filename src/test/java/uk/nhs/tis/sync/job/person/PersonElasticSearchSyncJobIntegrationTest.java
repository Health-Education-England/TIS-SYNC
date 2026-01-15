package uk.nhs.tis.sync.job.person;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

import com.transformuk.hee.tis.tcs.service.repository.PersonElasticSearchRepository;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import uk.nhs.tis.sync.Application;

@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Disabled
public class PersonElasticSearchSyncJobIntegrationTest {

  @Autowired
  PersonElasticSearchSyncJob job;

  @Autowired
  private ElasticsearchOperations elasticSearchOperations;

  @Autowired
  private PersonElasticSearchRepository repo;

  private static final String ES_INDEX = "persons";

  @BeforeEach
  public void setUp() throws Exception {
    elasticSearchOperations.deleteIndex(ES_INDEX);
    assertThat("should have deleted the index", elasticSearchOperations.indexExists(ES_INDEX),
        is(false));
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testJobRun() {
    job.run("");
    int maxLoops = 1440, loops = 0;
    //Loop while the job is running up to 2 hours
    await().atMost(2, TimeUnit.HOURS)
        .atLeast(5, TimeUnit.SECONDS)
        .with().pollInterval(5, TimeUnit.SECONDS)
        .until(() -> !job.isCurrentlyRunning());
    assertThat("should the sync job is not currently running", job.isCurrentlyRunning(), not(true));
    assertThat("then the sync job should not have timed out", loops, lessThan(maxLoops));
    long size = repo.count();
    assertThat("should have created index in elasticSearch",
        elasticSearchOperations.indexExists(ES_INDEX), is(true));
    assertThat("should have synchronized data to elasticSearch", size, not(0));
  }
}
