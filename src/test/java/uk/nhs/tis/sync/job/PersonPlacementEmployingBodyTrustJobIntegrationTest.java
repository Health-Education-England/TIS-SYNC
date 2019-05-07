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
    job.deleteData();
    Assert.assertThat("should have prepared the empty synchronized database table", repo.findAll().size(), CoreMatchers.is(0));
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testJobRun() throws Exception{
    job.doPersonPlacementEmployingBodyFullSync();
    int size = 0;
    for (int i = 0; i < 3; i++) {
      Thread.sleep(5 * 60 * 1000L);
      size = repo.findAll().size();
      if (size > 0) {
        break;
      }
    }
    Assert.assertThat("should have data in the synchronized database table", size, CoreMatchers.not(0));
  }

}
