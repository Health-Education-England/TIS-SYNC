package uk.nhs.tis.sync.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.nhs.tis.sync.job.person.PersonElasticSearchSyncJob;

/**
 * Controller for triggering jobs manually by devs
 */
@RestController
@RequestMapping("/api")
public class JobResource {

  public static final String PERSON_PLACEMENT_EMPLOYING_BODY_TRUST_JOB = "personPlacementEmployingBodyTrustJob";
  public static final String PERSON_PLACEMENT_TRAINING_BODY_TRUST_JOB = "personPlacementTrainingBodyTrustJob";
  public static final String POST_EMPLOYING_BODY_TRUST_JOB = "postEmployingBodyTrustJob";
  public static final String POST_TRAINING_BODY_TRUST_JOB = "postTrainingBodyTrustJob";
  public static final String PERSON_ELASTIC_SEARCH_SYNC_JOB = "personElasticSearchSyncJob";
  public static final String PERSON_OWNER_REBUILD_JOB = "personOwnerRebuildJob";
  public static final String PERSON_RECORD_STATUS_JOB = "personRecordStatusJob";
  private static final String ALREADY_RUNNING = "{\"status\":\"already running\"}";
  private static final String JUST_STARTED = "{\"status\":\"just started\"}";
  private final PersonPlacementEmployingBodyTrustJob personPlacementEmployingBodyTrustJob;
  private final PersonPlacementTrainingBodyTrustJob personPlacementTrainingBodyTrustJob;
  private final PostEmployingBodyTrustJob postEmployingBodyTrustJob;
  private final PostTrainingBodyTrustJob postTrainingBodyTrustJob;
  private final PersonElasticSearchSyncJob personElasticSearchSyncJob;
  private final PersonOwnerRebuildJob personOwnerRebuildJob;
  private final PersonRecordStatusJob personRecordStatusJob;

  private final Map<String, UnaryOperator<String>> jobFunctionMap;

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

    //There is a generic pattern to the function, so we can provide the `isRunning` check and actual
    // function to invoke when it is false.  Those 2 functions are passed to a factory method.
    jobFunctionMap = new HashMap<>();
    jobFunctionMap.put(PERSON_PLACEMENT_EMPLOYING_BODY_TRUST_JOB,
        jobFunctionFactory(personPlacementEmployingBodyTrustJob::isCurrentlyRunning,
            s -> personPlacementEmployingBodyTrustJob.doPersonPlacementEmployingBodyFullSync()));
    jobFunctionMap.put(PERSON_PLACEMENT_TRAINING_BODY_TRUST_JOB,
        jobFunctionFactory(personPlacementTrainingBodyTrustJob::isCurrentlyRunning,
            s -> personPlacementTrainingBodyTrustJob.PersonPlacementTrainingBodyFullSync()));
    jobFunctionMap.put(POST_EMPLOYING_BODY_TRUST_JOB,
        jobFunctionFactory(postEmployingBodyTrustJob::isCurrentlyRunning,
            s -> postEmployingBodyTrustJob.PostEmployingBodyTrustFullSync()));
    jobFunctionMap.put(POST_TRAINING_BODY_TRUST_JOB,
        jobFunctionFactory(postTrainingBodyTrustJob::isCurrentlyRunning,
            s -> postTrainingBodyTrustJob.PostTrainingBodyTrustFullSync()));
    jobFunctionMap.put(PERSON_ELASTIC_SEARCH_SYNC_JOB,
        jobFunctionFactory(personElasticSearchSyncJob::isCurrentlyRunning,
            s -> personElasticSearchSyncJob.personElasticSearchSync()));
    jobFunctionMap.put(PERSON_OWNER_REBUILD_JOB,
        jobFunctionFactory(personOwnerRebuildJob::isCurrentlyRunning,
            s -> personOwnerRebuildJob.personOwnerRebuildJob()));
    jobFunctionMap.put(PERSON_RECORD_STATUS_JOB,
        jobFunctionFactory(personRecordStatusJob::isCurrentlyRunning,
            personRecordStatusJob::personRecordStatusJob));
  }

  /**
   * GET /jobs/status : Get all the status of all 6 jobs
   *
   * @return the map of all the status. eg.{"personPlacementEmployingBodyTrustJob", "true"}, which
   *     means personPlacementEmployingBodyTrustJob is currently running.
   */
  @GetMapping("/jobs/status")
  @PreAuthorize("hasPermission('tis:sync::jobs:', 'View')")
  public ResponseEntity<Map<String, Boolean>> getStatus() {
    Map<String, Boolean> statusMap = new HashMap<>();
    statusMap.put(PERSON_PLACEMENT_EMPLOYING_BODY_TRUST_JOB,
        personPlacementEmployingBodyTrustJob.isCurrentlyRunning());
    statusMap.put(PERSON_PLACEMENT_TRAINING_BODY_TRUST_JOB,
        personPlacementTrainingBodyTrustJob.isCurrentlyRunning());
    statusMap.put(POST_EMPLOYING_BODY_TRUST_JOB, postEmployingBodyTrustJob.isCurrentlyRunning());
    statusMap.put(POST_TRAINING_BODY_TRUST_JOB, postTrainingBodyTrustJob.isCurrentlyRunning());
    statusMap.put(PERSON_ELASTIC_SEARCH_SYNC_JOB, personElasticSearchSyncJob.isCurrentlyRunning());
    statusMap.put(PERSON_OWNER_REBUILD_JOB, personOwnerRebuildJob.isCurrentlyRunning());
    statusMap.put(PERSON_RECORD_STATUS_JOB, personRecordStatusJob.isCurrentlyRunning());
    return ResponseEntity.ok().body(statusMap);
  }

  /**
   * PUT /jobs : Trigger the sequentially running of all the jobs
   */
  @PutMapping("/jobs")
  @PreAuthorize("hasPermission('tis:sync::jobs:', 'Update')")
  public ResponseEntity<Void> runJobsSequentially() {
    LOG.debug("REST request to run all jobs sequentially");
    CompletableFuture.runAsync(jobRunningListener::runJobs);
    return ResponseEntity.ok().build();
  }

  /**
   * PUT /job/:name : Trigger one individual job
   *
   * @param name the name of the job to run
   * @return status of the requested job : "already running" - the job has been running before
   *     triggering it "just started" - the job has been started by this request
   */
  @PutMapping("/job/{name}")
  @PreAuthorize("hasPermission('tis:sync::jobs:', 'Update')")
  public ResponseEntity<String> runJob(@PathVariable String name,
      @RequestBody(required = false) String params) {
    LOG.debug("REST request to run job: {}", name);

    UnaryOperator<String> jobFunction = jobFunctionMap.get(name);
    if (jobFunction == null) {
      return ResponseEntity.badRequest().body("{\"error\":\"job not found\"}");
    } else {
      try {
        return ResponseEntity.ok().body(jobFunction.apply(params));
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(String.format("{\"error\":\"%s\"}",
            e.getMessage()));
      }
    }
  }

  private UnaryOperator<String> jobFunctionFactory(BooleanSupplier ifRunningClause,
      Consumer<String> falseFunction) {
    return s -> {
      if (ifRunningClause.getAsBoolean()) {
        return ALREADY_RUNNING;
      } else {
        falseFunction.accept(s);
        return JUST_STARTED;
      }
    };
  }
}
