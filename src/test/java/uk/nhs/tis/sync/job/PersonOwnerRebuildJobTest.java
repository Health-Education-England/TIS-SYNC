package uk.nhs.tis.sync.job;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.transformuk.hee.tis.tcs.service.repository.PersonRepository;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PersonOwnerRebuildJobTest {

  @Autowired
  private PersonOwnerRebuildJob job;

  @MockBean
  private PersonRepository repo;

  @Test
  public void testPersonOwnerRebuildJob() {
    job.run("");
    with().pollDelay(1, TimeUnit.SECONDS).await().atLeast(1, TimeUnit.SECONDS).until(() -> true);
    verify(repo).buildPersonView();
    assertThat("should not be running", job.isCurrentlyRunning(), is(false));
  }

  @Test
  public void testElapsedTimeAndRunningWhenNotRunning() {
    //Combining as they are
    boolean running = job.isCurrentlyRunning();
    String runningTime = job.elapsedTime();
    assertFalse(running);
    assertEquals("0s", runningTime);
  }

  @Test
  public void testElapsedTimeAndRunningAndAddedInvokeWhenRunning() {
    doAnswer(invocation -> {
      System.out.println("Before" + LocalDateTime.now());
      with().pollDelay(2, TimeUnit.SECONDS).atLeast(2, TimeUnit.SECONDS).until(() -> true);
      System.out.println("After" + LocalDateTime.now());
      return null;
    }).when(repo).buildPersonView();
    job.run("");
    with().pollDelay(1, TimeUnit.SECONDS).atLeast(1, TimeUnit.SECONDS).until(() -> true);
    boolean running = job.isCurrentlyRunning();
    String runningTime = job.elapsedTime();
    //rerun to check that we don't get a duplicate invocation
    assertThat("should be running", running, is(true));
    assertNotEquals("should not be zero", "0s", runningTime);
  }

  @Test
  public void testCoverageBoostWhenRepoThrowsException() {
    doThrow(new RuntimeException("Expected")).when(repo).buildPersonView();
    job.run("");
    await().pollDelay(1, TimeUnit.SECONDS).atLeast(1, TimeUnit.SECONDS).until(() -> true);
    assertThat("should not be running", job.isCurrentlyRunning(), is(false));
  }
}
