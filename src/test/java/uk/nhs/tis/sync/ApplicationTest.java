package uk.nhs.tis.sync;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import uk.nhs.tis.sync.job.RecordResendingJob;

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
