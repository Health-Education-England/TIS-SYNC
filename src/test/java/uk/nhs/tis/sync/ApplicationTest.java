package uk.nhs.tis.sync;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.tis.sync.job.SyncHandlingJob;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

  @Autowired
  Application testClass;

  // Mock the sync handler as it requires an SQS queue to be accessible.
  @MockBean
  private SyncHandlingJob syncHandlingJob;

  @Test
  public void testContextLoads() {
  }

}
