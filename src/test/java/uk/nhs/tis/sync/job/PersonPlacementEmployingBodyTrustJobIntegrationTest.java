package uk.nhs.tis.sync.job;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.nhs.tis.sync.job.PersonPlacementEmployingBodyTrustJob;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class PersonPlacementEmployingBodyTrustJobIntegrationTest {
  
  @Autowired
  PersonPlacementEmployingBodyTrustJob job;

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testJobRun() {
    //assert ElasticSearch is clean (or at least doesn't have the data we are inserting)
    
    job.doPersonPlacementEmployingBodyFullSync();
  }

}
