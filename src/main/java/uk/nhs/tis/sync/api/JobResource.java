package uk.nhs.tis.sync.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import uk.nhs.tis.sync.event.listener.JobRunningListener;
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
  private PersonRecordStatusJob personRecordStatusJob;

  @Autowired
  private JobRunningListener jobRunningListener;

  private static final Logger LOG = LoggerFactory.getLogger(JobResource.class);

  public JobResource(PersonPlacementEmployingBodyTrustJob personPlacementEmployingBodyTrustJob,
                     PersonPlacementTrainingBodyTrustJob personPlacementTrainingBodyTrustJob,
                     PostEmployingBodyTrustJob postEmployingBodyTrustJob,
                     PostTrainingBodyTrustJob postTrainingBodyTrustJob,
                     PersonElasticSearchSyncJob personElasticSearchSyncJob,
                     PersonOwnerRebuildJob personOwnerRebuildJob,
                     PersonRecordStatusJob personRecordStatusJob) {
    this.personPlacementEmployingBodyTrustJob = personPlacementEmployingBodyTrustJob;
    this.personPlacementTrainingBodyTrustJob = personPlacementTrainingBodyTrustJob;
    this.postEmployingBodyTrustJob = postEmployingBodyTrustJob;
    this.postTrainingBodyTrustJob = postTrainingBodyTrustJob;
    this.personElasticSearchSyncJob = personElasticSearchSyncJob;
    this.personOwnerRebuildJob = personOwnerRebuildJob;
    this.personRecordStatusJob = personRecordStatusJob;
  }

  /**
   * GET /jobs/status : Get all the status of all 6 jobs
   *
   * @return the map of all the status.
   * eg.{"personPlacementEmployingBodyTrustJob", "true"}, which means personPlacementEmployingBodyTrustJob is currently running.
   */
  @GetMapping("/jobs/status")
  @PreAuthorize("hasPermission('tis:sync::jobs:', 'View')")
  public ResponseEntity<Map> getStatus() {
    Map<String, Boolean> statusMap = new HashMap<>();
    statusMap.put("personPlacementEmployingBodyTrustJob", personPlacementEmployingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("personPlacementTrainingBodyTrustJob", personPlacementTrainingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("postEmployingBodyTrustJob", postEmployingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("postTrainingBodyTrustJob", postTrainingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("personElasticSearchSyncJob", personElasticSearchSyncJob.isCurrentlyRunning());
    statusMap.put("personOwnerRebuildJob", personOwnerRebuildJob.isCurrentlyRunning());
    statusMap.put("personRecordStatusJob", personRecordStatusJob.isCurrentlyRunning());
    return ResponseEntity.ok().body(statusMap);
  }

  /**
   * PUT /jobs : Trigger the sequentially running of all the jobs
   */
  @PutMapping("/jobs")
  @PreAuthorize("hasPermission('tis:sync::jobs:', 'Update')")
  public ResponseEntity<Void> runJobsSequentially() {
    LOG.debug("REST reqeust to run all jobs sequentially");
    CompletableFuture.runAsync(jobRunningListener::runJobs);
    return ResponseEntity.ok().build();
  }

  /**
   * PUT /job/:name : Trigger one individual job
   *
   * @param name the name of the job to run
   * @return status of the requested job :
   * "already running" - the job has been running before triggering it
   * "just started" - the job has been started by this request
   */
  @PutMapping("/job/{name}")
  @PreAuthorize("hasPermission('tis:sync::jobs:', 'Update')")
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
      case "personRecordStatusJob":
        if (personRecordStatusJob.isCurrentlyRunning()) {
          status = ALREADY_RUNNING;
        } else {
          personRecordStatusJob.personRecordStatusJob();
        }
        break;
      default:
        return ResponseEntity.badRequest().body("{\"error\":\"job not found\"}");
    }
    return ResponseEntity.ok().body(status);
  }
}
