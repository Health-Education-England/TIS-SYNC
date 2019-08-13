package uk.nhs.tis.sync.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.nhs.tis.sync.JobRunningListener;
import uk.nhs.tis.sync.job.*;
import uk.nhs.tis.sync.job.person.PersonElasticSearchSyncJob;

import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.CompletableFuture;

/**
 * Controller for triggering jobs manually by devs
 */
@RestController
@RequestMapping("/api")
public class JobResource {

  private PersonPlacementEmployingBodyTrustJob personPlacementEmployingBodyTrustJob;
  private PersonPlacementTrainingBodyTrustJob personPlacementTrainingBodyTrustJob;
  private PostEmployingBodyTrustJob postEmployingBodyTrustJob;
  private PostTrainingBodyTrustJob postTrainingBodyTrustJob;
  private PersonElasticSearchSyncJob personElasticSearchSyncJob;
  private PersonOwnerRebuildJob personOwnerRebuildJob;

  @Autowired
  private JobRunningListener jobRunningListener;

  private static final Logger LOG = LoggerFactory.getLogger(JobResource.class);

  public JobResource(PersonPlacementEmployingBodyTrustJob personPlacementEmployingBodyTrustJob,
                     PersonPlacementTrainingBodyTrustJob personPlacementTrainingBodyTrustJob,
                     PostEmployingBodyTrustJob postEmployingBodyTrustJob,
                     PostTrainingBodyTrustJob postTrainingBodyTrustJob,
                     PersonElasticSearchSyncJob personElasticSearchSyncJob,
                     PersonOwnerRebuildJob personOwnerRebuildJob) {
    this.personPlacementEmployingBodyTrustJob = personPlacementEmployingBodyTrustJob;
    this.personPlacementTrainingBodyTrustJob = personPlacementTrainingBodyTrustJob;
    this.postEmployingBodyTrustJob = postEmployingBodyTrustJob;
    this.postTrainingBodyTrustJob = postTrainingBodyTrustJob;
    this.personElasticSearchSyncJob = personElasticSearchSyncJob;
    this.personOwnerRebuildJob = personOwnerRebuildJob;
  }

  @GetMapping("/jobs/status")
  public ResponseEntity<Map> getStatus() {
    Map<String, Boolean> statusMap = new HashMap<>();
    statusMap.put("personPlacementEmployingBodyTrustJob", personPlacementEmployingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("personPlacementTrainingBodyTrustJob", personPlacementTrainingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("postEmployingBodyTrustJob", postEmployingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("postTrainingBodyTrustJob", postTrainingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("personElasticSearchSyncJob", personElasticSearchSyncJob.isCurrentlyRunning());
    statusMap.put("personOwnerRebuildJob", personOwnerRebuildJob.isCurrentlyRunning());
    return ResponseEntity.ok().body(statusMap);
  }

  @PutMapping("/jobs")
  public ResponseEntity<Void> runJobsSequentially() {
    LOG.debug("REST reqeust to run all jobs sequentially");
    CompletableFuture.runAsync(jobRunningListener::runJobs);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/job/{name}")
  public ResponseEntity<String> runJob(@PathVariable String name) {
    LOG.debug("REST reqeust to run job: {}", name);
    final String ALREADY_RUNNING = "{\"status\":\"already running\"}";
    final String JUST_STARTED = "{\"status\":\"just started\"}";

    String status = JUST_STARTED;

    switch (name) {
      case "personPlacementEmployingBodyTrustJob":
        if (personPlacementEmployingBodyTrustJob.isCurrentlyRunning()) {
          status = ALREADY_RUNNING;
        } else {
          personPlacementEmployingBodyTrustJob.doPersonPlacementEmployingBodyFullSync();
        }
        break;
      case "personPlacementTrainingBodyTrustJob":
        if (personPlacementTrainingBodyTrustJob.isCurrentlyRunning()) {
          status = ALREADY_RUNNING;
        } else {
          personPlacementTrainingBodyTrustJob.PersonPlacementTrainingBodyFullSync();
        }
        break;
      case "postEmployingBodyTrustJob":
        if (postEmployingBodyTrustJob.isCurrentlyRunning()) {
          status = ALREADY_RUNNING;
        } else {
          postEmployingBodyTrustJob.PostEmployingBodyTrustFullSync();
        }
        break;
      case "postTrainingBodyTrustJob":
        if (postTrainingBodyTrustJob.isCurrentlyRunning()) {
          status = ALREADY_RUNNING;
        } else {
          postTrainingBodyTrustJob.PostTrainingBodyTrustFullSync();
        }
        break;
      case "personElasticSearchSyncJob":
        if (personElasticSearchSyncJob.isCurrentlyRunning()) {
          status = ALREADY_RUNNING;
        } else {
          personElasticSearchSyncJob.personElasticSearchSync();
        }
        break;
      case "personOwnerRebuildJob":
        if (personOwnerRebuildJob.isCurrentlyRunning()) {
          status = ALREADY_RUNNING;
        } else {
          personOwnerRebuildJob.personOwnerRebuildJob();
        }
        break;
      default:
        return ResponseEntity.badRequest().body("{\"error\":\"job not found\"}");
    }
    return ResponseEntity.ok().body(status);
  }
}
