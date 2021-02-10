package uk.nhs.tis.sync;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.tis.sync.job.RecordResendingJob;
import uk.nhs.tis.sync.service.DmsRecordAssembler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

  @Autowired
  Application testClass;

  @Autowired
  ApplicationContext applicationContext;

  // Mock the sync handler as it requires an SQS queue to be accessible.
  @MockBean
  private RecordResendingJob recordResendingJob;

  @MockBean
  private DmsRecordAssembler dmsRecordAssembler;

  @Test
  public void testContextLoads() {
    assertThat("Unexpected bean.", applicationContext.getBean(RecordResendingJob.class), is(recordResendingJob));
  }

}
