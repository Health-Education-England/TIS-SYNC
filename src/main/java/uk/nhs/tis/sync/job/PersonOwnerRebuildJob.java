package uk.nhs.tis.sync.job;

import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.google.common.base.Stopwatch;
import com.transformuk.hee.tis.tcs.service.repository.PersonRepository;
import net.javacrumbs.shedlock.core.SchedulerLock;
import uk.nhs.tis.sync.event.JobExecutionEvent;

@Component
@ManagedResource(objectName = "sync.mbean:name=PersonOwnerRebuildJob",
    description = "Job that clears and recreates the PersonOwner table")
public class PersonOwnerRebuildJob {

  private static final Logger LOG = LoggerFactory.getLogger(PersonOwnerRebuildJob.class);

  private static final int FIFTEEN_MIN = 15 * 60 * 1000;

  @Autowired
  private PersonRepository personRepository;

  @Autowired(required = false)
  private ApplicationEventPublisher applicationEventPublisher;

  private Stopwatch mainStopWatch;

  @Scheduled(cron = "${application.cron.personOwnerRebuildJob}")
  @SchedulerLock(name = "personOwnerRebuildScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(description = "Run sync of the PersonOwner table")
  public void personOwnerRebuildJob() {
    runSyncJob();
  }

  protected void runSyncJob() {
    if (mainStopWatch != null) {
      LOG.info("Sync job [{}] already running, exiting this execution", getJobName());
      return;
    }
    CompletableFuture.runAsync(this::run);
  }

  protected void run() {
    try {
      LOG.info("Sync [{}] started", getJobName());
      if (applicationEventPublisher != null) {
        applicationEventPublisher
            .publishEvent(new JobExecutionEvent(this, "Sync [" + getJobName() + "] started."));
      }
      mainStopWatch = Stopwatch.createStarted();

      personRepository.buildPersonView();

      LOG.info("Sync job [{}] finished. Total time taken {} to rebuild the table", getJobName(),
          mainStopWatch.stop().toString());
      mainStopWatch = null;
      if (applicationEventPublisher != null) {
        applicationEventPublisher
            .publishEvent(new JobExecutionEvent(this, "Sync [" + getJobName() + "] finished."));
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      mainStopWatch = null;
      if (applicationEventPublisher != null) {
        applicationEventPublisher.publishEvent(new JobExecutionEvent(this, "<!channel> Sync ["
            + getJobName() + "] failed with exception [" + e.getMessage() + "]."));
      }
      throw e;
    }
  }

  private String getJobName() {
    return "PersonOwnerJob";
  }

  @ManagedOperation(description = "Is the Person Owner Rebuild job currently running")
  public boolean isCurrentlyRunning() {
    return mainStopWatch != null;
  }

  @ManagedOperation(description = "The current elapsed time of the current sync job")
  public String elapsedTime() {
    return mainStopWatch != null ? mainStopWatch.toString() : "0s";
  }

}
