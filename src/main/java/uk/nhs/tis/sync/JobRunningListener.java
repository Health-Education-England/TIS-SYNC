/**
 * 
 */
package uk.nhs.tis.sync;

import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.job.PersonOwnerRebuildJob;
import uk.nhs.tis.sync.job.PersonPlacementEmployingBodyTrustJob;
import uk.nhs.tis.sync.job.PersonPlacementTrainingBodyTrustJob;
import uk.nhs.tis.sync.job.PostEmployingBodyTrustJob;
import uk.nhs.tis.sync.job.PostTrainingBodyTrustJob;
import uk.nhs.tis.sync.job.person.PersonElasticSearchSyncJob;

@Component
public class JobRunningListener implements ApplicationListener<ApplicationReadyEvent> {

  private static final Logger LOG = LoggerFactory.getLogger(JobRunningListener.class);

  private static final long SLEEP_DURATION = 5 * 1000;

  @Autowired
  private PersonOwnerRebuildJob personOwnerRebuildJob;

  @Autowired
  private PersonPlacementEmployingBodyTrustJob personPlacementEmployingBodyTrustJob;

  @Autowired
  private PersonPlacementTrainingBodyTrustJob personPlacementTrainingBodyTrustJob;

  @Autowired
  private PostEmployingBodyTrustJob postEmployingBodyTrustJob;

  @Autowired
  private PostTrainingBodyTrustJob postTrainingBodyTrustJob;

  @Autowired
  private PersonElasticSearchSyncJob personElasticSearchSyncJob;

  @Value("${application.jobs.runOnStartup.earliest}")
  private LocalTime earliest;

  @Value("${application.jobs.runOnStartup.latest}")
  private LocalTime latest;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    LOG.debug("Received event for an {}", event.getClass().getSimpleName());
    LocalTime time = LocalTime.now();
    if (time.isAfter(earliest) && time.isBefore(latest)) {
      LOG.info("Attempting to run jobs as Application started between {} and {}", earliest, latest);
      runJobs();
    }
  }

  public void runJobs() {
    try {
      personOwnerRebuildJob.personOwnerRebuildJob();
      do {
        Thread.sleep(SLEEP_DURATION);
      } while (personOwnerRebuildJob.isCurrentlyRunning());
      personPlacementEmployingBodyTrustJob.doPersonPlacementEmployingBodyFullSync();
      do {
        Thread.sleep(SLEEP_DURATION);
      } while (personPlacementEmployingBodyTrustJob.isCurrentlyRunning());
      personPlacementTrainingBodyTrustJob.PersonPlacementTrainingBodyFullSync();
      do {
        Thread.sleep(SLEEP_DURATION);
      } while (personPlacementTrainingBodyTrustJob.isCurrentlyRunning());
      postEmployingBodyTrustJob.PostEmployingBodyTrustFullSync();
      do {
        Thread.sleep(SLEEP_DURATION);
      } while (postEmployingBodyTrustJob.isCurrentlyRunning());
      postTrainingBodyTrustJob.PostTrainingBodyTrustFullSync();
      do {
        Thread.sleep(SLEEP_DURATION);
      } while (postTrainingBodyTrustJob.isCurrentlyRunning());
      personElasticSearchSyncJob.personElasticSearchSync();
      do {
        Thread.sleep(SLEEP_DURATION);
      } while (personElasticSearchSyncJob.isCurrentlyRunning());
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  @Value("${application.jobs.runOnStartup.earliest}")
  public void setEarliest(LocalTime earliest) {
    this.earliest = earliest;
  }

  @Value("${application.jobs.runOnStartup.latest}")
  public void setLatest(LocalTime latest) {
    this.latest = latest;
  }
}
