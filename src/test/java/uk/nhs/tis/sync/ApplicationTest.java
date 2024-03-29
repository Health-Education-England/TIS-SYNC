package uk.nhs.tis.sync;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import uk.nhs.tis.sync.job.RecordResendingJob;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTest {

  @Autowired
  Application testClass;

  @Autowired
  ApplicationContext applicationContext;

  @Test
  public void testContextLoads() {
    assertThat("Unexpected bean.", applicationContext.getBean(RecordResendingJob.class),
    notNullValue(RecordResendingJob.class));
  }

}
