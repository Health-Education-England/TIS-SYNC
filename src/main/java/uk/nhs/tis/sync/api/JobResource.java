package uk.nhs.tis.sync.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.nhs.tis.sync.JobRunningListener;
import uk.nhs.tis.sync.job.PersonPlacementEmployingBodyTrustJob;
import uk.nhs.tis.sync.job.PersonPlacementTrainingBodyTrustJob;
import uk.nhs.tis.sync.job.PostEmployingBodyTrustJob;
import uk.nhs.tis.sync.job.PostTrainingBodyTrustJob;
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

  @Autowired
  private JobRunningListener jobRunningListener;

  private static final Logger LOG = LoggerFactory.getLogger(JobResource.class);

  @GetMapping("/jobs/status")
  public ResponseEntity<Map> getStatus() {
    Map<String, Boolean> statusMap = new HashMap<>();
    statusMap.put("personPlacementEmployingBodyTrustJob", personPlacementEmployingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("personPlacementTrainingBodyTrustJob", personPlacementTrainingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("postEmployingBodyTrustJob", postEmployingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("postTrainingBodyTrustJob", postTrainingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("personElasticSearchSyncJob", personElasticSearchSyncJob.isCurrentlyRunning());
    return ResponseEntity.ok().body(statusMap);
  }

  @PutMapping("/jobs")
  public ResponseEntity<Void> runJobsSequentially() {
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
      default:
        return ResponseEntity.badRequest().body("{\"error\":\"job not found\"}");
    }
    return ResponseEntity.ok().body(status);
  }
}
