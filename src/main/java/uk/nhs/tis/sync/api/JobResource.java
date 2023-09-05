package uk.nhs.tis.sync.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.nhs.tis.sync.event.listener.JobRunningListener;
import uk.nhs.tis.sync.job.PersonOwnerRebuildJob;
import uk.nhs.tis.sync.job.PersonPlacementEmployingBodyTrustJob;
import uk.nhs.tis.sync.job.PersonPlacementTrainingBodyTrustJob;
import uk.nhs.tis.sync.job.PersonRecordStatusJob;
import uk.nhs.tis.sync.job.PostEmployingBodyTrustJob;
import uk.nhs.tis.sync.job.PostTrainingBodyTrustJob;
import uk.nhs.tis.sync.job.RunnableJob;
import uk.nhs.tis.sync.job.person.PersonElasticSearchSyncJob;
import uk.nhs.tis.sync.job.reval.RevalCurrentPlacementSyncJob;
import uk.nhs.tis.sync.job.reval.RevalCurrentPmSyncJob;

/**
 * Controller for triggering jobs manually by devs
 */
@RestController
@RequestMapping("/api")
@Slf4j
public class JobResource {

  private static final String ALREADY_RUNNING = "{\"status\":\"Already running\"}";
  private static final String JUST_STARTED = "{\"status\":\"Just started\"}";
  private static final String JOB_NOT_FOUND = "{\"error\":\"Job not found\"}";

  private final PersonPlacementEmployingBodyTrustJob personPlacementEmployingBodyTrustJob;
  private final PersonPlacementTrainingBodyTrustJob personPlacementTrainingBodyTrustJob;
  private final PostEmployingBodyTrustJob postEmployingBodyTrustJob;
  private final PostTrainingBodyTrustJob postTrainingBodyTrustJob;
  private final PersonElasticSearchSyncJob personElasticSearchSyncJob;
  private final PersonOwnerRebuildJob personOwnerRebuildJob;
  private final PersonRecordStatusJob personRecordStatusJob;
  private RevalCurrentPmSyncJob revalCurrentPmSyncJob;
  private RevalCurrentPlacementSyncJob revalCurrentPlacementSyncJob;
  @Autowired
  private JobRunningListener jobRunningListener;
  @Value("${spring.profiles.active:}")
  private String activeProfile;

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

  @Autowired(required = false)
  public void setRevalCurrentPmSyncJob(RevalCurrentPmSyncJob revalCurrentPmSyncJob) {
    this.revalCurrentPmSyncJob = revalCurrentPmSyncJob;
  }

  @Autowired(required = false)
  public void setRevalCurrentPlacementSyncJob(RevalCurrentPlacementSyncJob revalCurrentPlacementSyncJob) {
    this.revalCurrentPlacementSyncJob = revalCurrentPlacementSyncJob;
  }

  /**
   * GET /jobs/status : Get all the status of 8 jobs.
   *
   * @return map of the status for most jobs. eg. {"personPlacementEmployingBodyTrustJob", "true"},
   *     which means personPlacementEmployingBodyTrustJob is currently running.
   */
  @GetMapping("/jobs/status")
  @PreAuthorize("hasPermission('tis:sync::jobs:', 'View')")
  public ResponseEntity<Map<String, Boolean>> getStatus() {
    Map<String, Boolean> statusMap = new HashMap<>();
    statusMap.put("personPlacementEmployingBodyTrustJob",
        personPlacementEmployingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("personPlacementTrainingBodyTrustJob",
        personPlacementTrainingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("postEmployingBodyTrustJob", postEmployingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("postTrainingBodyTrustJob", postTrainingBodyTrustJob.isCurrentlyRunning());
    statusMap.put("personElasticSearchSyncJob", personElasticSearchSyncJob.isCurrentlyRunning());
    statusMap.put("personOwnerRebuildJob", personOwnerRebuildJob.isCurrentlyRunning());
    statusMap.put("personRecordStatusJob", personRecordStatusJob.isCurrentlyRunning());
    if (revalCurrentPmSyncJob != null) {
      statusMap.put("revalCurrentPmJob", revalCurrentPmSyncJob.isCurrentlyRunning());
    }
    if (revalCurrentPlacementSyncJob != null) {
      statusMap.put("revalCurrentPlacementJob", revalCurrentPlacementSyncJob.isCurrentlyRunning());
    }
    return ResponseEntity.ok().body(statusMap);
  }

  @GetMapping("/sys/profile")
  public ResponseEntity<String> getSysProfile() {
    return ResponseEntity.ok(activeProfile);
  }

  /**
   * PUT /jobs : Trigger the sequentially running of all the jobs
   */
  @PutMapping("/jobs")
  @PreAuthorize("hasPermission('tis:sync::jobs:', 'Update')")
  public ResponseEntity<Void> runJobsSequentially() {
    log.debug("REST request to run all jobs sequentially");
    CompletableFuture.runAsync(jobRunningListener::runJobs);
    return ResponseEntity.ok().build();
  }

  /**
   * PUT /job/:name : Trigger one individual job
   *
   * @param name the name of the job to run
   * @return status of the requested job : "Already running" - the job has been running before
   *     triggering it "Just started" - the job has been started by this request
   */
  @PutMapping("/job/{name}")
  @PreAuthorize("hasPermission('tis:sync::jobs:', 'Update')")
  public ResponseEntity<String> runJob(@PathVariable String name,
      @RequestBody(required = false) String params) {
    log.debug("REST request to run job: {}", name);

    String status;

    switch (name) {
      case "personPlacementEmployingBodyTrustJob":
        status = ensureRunning(personPlacementEmployingBodyTrustJob, params);
        break;
      case "personPlacementTrainingBodyTrustJob":
        status = ensureRunning(personPlacementTrainingBodyTrustJob, params);
        break;
      case "postEmployingBodyTrustJob":
        status = ensureRunning(postEmployingBodyTrustJob, params);
        break;
      case "postTrainingBodyTrustJob":
        status = ensureRunning(postTrainingBodyTrustJob, params);
        break;
      case "personElasticSearchSyncJob":
        status = ensureRunning(personElasticSearchSyncJob, params);
        break;
      case "personOwnerRebuildJob":
        status = ensureRunning(personOwnerRebuildJob, params);
        break;
      case "personRecordStatusJob":
        try {
          status = ensureRunning(personRecordStatusJob, params);
        } catch (IllegalArgumentException e) {
          return ResponseEntity.badRequest().body(String.format("{\"error\":\"%s\"}",
              e.getMessage()));
        }
        break;
      case "revalCurrentPmJob":
        if (revalCurrentPmSyncJob != null) {
          status = ensureRunning(revalCurrentPmSyncJob, params);
        } else {
          return ResponseEntity.badRequest().body(JOB_NOT_FOUND);
        }
        break;
      case "revalCurrentPlacementJob":
        if (revalCurrentPlacementSyncJob != null) {
          status = ensureRunning(revalCurrentPlacementSyncJob, params);
        } else {
          return ResponseEntity.badRequest().body(JOB_NOT_FOUND);
        }
        break;
      default:
        return ResponseEntity.badRequest().body(JOB_NOT_FOUND);
    }
    return ResponseEntity.ok().body(status);
  }

  private String ensureRunning(RunnableJob job, String params) {
    if (job.isCurrentlyRunning()) {
      return ALREADY_RUNNING;
    } else {
      job.run(params);
      return JUST_STARTED;
    }
  }
}
