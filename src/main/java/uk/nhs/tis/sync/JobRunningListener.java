/**
 * 
 */
package uk.nhs.tis.sync;

import java.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
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
  private PersonPlacementEmployingBodyTrustJob personPlacementEmployingBodyTrustJob;

  @Autowired
  private PersonPlacementTrainingBodyTrustJob personPlacementTrainingBodyTrustJob;

  @Autowired
  private PostEmployingBodyTrustJob postEmployingBodyTrustJob;

  @Autowired
  private PostTrainingBodyTrustJob postTrainingBodyTrustJob;

  @Autowired
  private PersonElasticSearchSyncJob personElasticSearchSyncJob;

  private LocalTime earliest = LocalTime.of(0, 0, 0);

  private LocalTime latest = LocalTime.of(7, 0, 0);

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
      personPlacementEmployingBodyTrustJob.doPersonPlacementEmployingBodyFullSync();
      Thread.sleep(SLEEP_DURATION);
      while (personPlacementEmployingBodyTrustJob.isCurrentlyRunning()) {
        Thread.sleep(SLEEP_DURATION);
      }
      personPlacementTrainingBodyTrustJob.PersonPlacementTrainingBodyFullSync();
      Thread.sleep(SLEEP_DURATION);
      while (personPlacementTrainingBodyTrustJob.isCurrentlyRunning()) {
        Thread.sleep(SLEEP_DURATION);
      }
      postEmployingBodyTrustJob.PostEmployingBodyTrustFullSync();
      Thread.sleep(SLEEP_DURATION);
      while (postEmployingBodyTrustJob.isCurrentlyRunning()) {
        Thread.sleep(SLEEP_DURATION);
      }
      postTrainingBodyTrustJob.PostTrainingBodyTrustFullSync();;
      Thread.sleep(SLEEP_DURATION);
      while (postTrainingBodyTrustJob.isCurrentlyRunning()) {
        Thread.sleep(SLEEP_DURATION);
      }
      personElasticSearchSyncJob.personElasticSearchSync();
      Thread.sleep(SLEEP_DURATION);
      while (personElasticSearchSyncJob.isCurrentlyRunning()) {
        Thread.sleep(SLEEP_DURATION);
      }
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }
  }
}
