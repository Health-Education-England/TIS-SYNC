package uk.nhs.tis.sync.job;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import uk.nhs.tis.sync.job.PersonPlacementEmployingBodyTrustJob;

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
