package uk.nhs.tis.sync.api;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.ws.rs.core.MediaType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.nhs.tis.sync.event.listener.JobRunningListener;
import uk.nhs.tis.sync.job.PersonOwnerRebuildJob;
import uk.nhs.tis.sync.job.PersonPlacementEmployingBodyTrustJob;
import uk.nhs.tis.sync.job.PersonPlacementTrainingBodyTrustJob;
import uk.nhs.tis.sync.job.PersonRecordStatusJob;
import uk.nhs.tis.sync.job.PostEmployingBodyTrustJob;
import uk.nhs.tis.sync.job.PostFundingSyncJob;
import uk.nhs.tis.sync.job.PostTrainingBodyTrustJob;
import uk.nhs.tis.sync.job.person.PersonElasticSearchSyncJob;
import uk.nhs.tis.sync.job.reval.RevalCurrentPlacementSyncJob;
import uk.nhs.tis.sync.job.reval.RevalCurrentPmSyncJob;

@DisplayName("JobResourceTest")
@ExtendWith(SpringExtension.class)
class JobResourceTest {

  @MockBean
  private PersonPlacementEmployingBodyTrustJob personPlacementEmployingBodyTrustJob;

  @MockBean
  private PersonPlacementTrainingBodyTrustJob personPlacementTrainingBodyTrustJob;

  @MockBean
  private PostEmployingBodyTrustJob postEmployingBodyTrustJob;

  @MockBean
  private PostTrainingBodyTrustJob postTrainingBodyTrustJob;

  @MockBean
  private PersonElasticSearchSyncJob personElasticSearchSyncJob;

  @MockBean
  private PersonOwnerRebuildJob personOwnerRebuildJob;

  @MockBean
  private PersonRecordStatusJob personRecordStatusJob;

  @MockBean
  private RevalCurrentPmSyncJob revalCurrentPmSyncJob;
  @MockBean
  private RevalCurrentPlacementSyncJob revalCurrentPlacementSyncJob;

  @MockBean
  PostFundingSyncJob postFundingSyncJob;

  private MockMvc mockMvc;

  private JobResource jobResource;

  @BeforeEach
  void setup() {
    jobResource = new JobResource(personPlacementEmployingBodyTrustJob,
        personPlacementTrainingBodyTrustJob,
        postEmployingBodyTrustJob,
        postTrainingBodyTrustJob,
        personElasticSearchSyncJob,
        personOwnerRebuildJob,
        personRecordStatusJob);
    jobResource.setRevalCurrentPmSyncJob(revalCurrentPmSyncJob);
    jobResource.setRevalCurrentPlacementSyncJob((revalCurrentPlacementSyncJob));
    jobResource.setPostFundingSyncJob(postFundingSyncJob);
    mockMvc = MockMvcBuilders.standaloneSetup(jobResource).build();
  }

  @DisplayName("get all Status")
  @Test
  void shouldReturnAllStatusWhenGetStatus() throws Exception {
    when(personPlacementEmployingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(false);
    when(personPlacementTrainingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(true);
    when(postEmployingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(false);
    when(postTrainingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(false);
    when(personElasticSearchSyncJob.isCurrentlyRunning())
        .thenReturn(false);
    when(personOwnerRebuildJob.isCurrentlyRunning())
        .thenReturn(false);
    when(personRecordStatusJob.isCurrentlyRunning())
        .thenReturn(false);
    when(revalCurrentPmSyncJob.isCurrentlyRunning())
        .thenReturn(false);
    when(postFundingSyncJob.isCurrentlyRunning())
        .thenReturn(false);

    mockMvc.perform(get("/api/jobs/status")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.personPlacementTrainingBodyTrustJob").value(true))
        .andExpect(jsonPath("$.personPlacementEmployingBodyTrustJob").value(false))
        .andExpect(jsonPath("$.postEmployingBodyTrustJob").value(false))
        .andExpect(jsonPath("$.postTrainingBodyTrustJob").value(false))
        .andExpect(jsonPath("$.personElasticSearchSyncJob").value(false))
        .andExpect(jsonPath("$.personOwnerRebuildJob").value(false))
        .andExpect(jsonPath("$.personRecordStatusJob").value(false))
        .andExpect(jsonPath("$.revalCurrentPmJob").value(false))
        .andExpect(jsonPath("$.postFundingSyncJob").value(false))
        .andExpect(status().isOk());
  }

  @Test
  void shouldNotReturnStatusForRevalCurrentSyncWhenJobNotAvailable() throws Exception {
    jobResource.setRevalCurrentPmSyncJob(null);
    jobResource.setRevalCurrentPlacementSyncJob(null);
    when(personPlacementEmployingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(false);
    when(personPlacementTrainingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(true);
    when(postEmployingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(false);
    when(postTrainingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(false);
    when(personElasticSearchSyncJob.isCurrentlyRunning())
        .thenReturn(false);
    when(personOwnerRebuildJob.isCurrentlyRunning())
        .thenReturn(false);
    when(personRecordStatusJob.isCurrentlyRunning())
        .thenReturn(false);

    mockMvc.perform(get("/api/jobs/status")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasKey("personPlacementEmployingBodyTrustJob")))
        .andExpect(jsonPath("$", hasKey("personPlacementTrainingBodyTrustJob")))
        .andExpect(jsonPath("$", hasKey("postEmployingBodyTrustJob")))
        .andExpect(jsonPath("$", hasKey("postTrainingBodyTrustJob")))
        .andExpect(jsonPath("$", hasKey("personElasticSearchSyncJob")))
        .andExpect(jsonPath("$", hasKey("personOwnerRebuildJob")))
        .andExpect(jsonPath("$", hasKey("personRecordStatusJob")))
        .andExpect(jsonPath("$", not(hasKey("revalCurrentPmSyncJob"))))
        .andExpect(status().isOk());
  }

  @DisplayName("run a job")
  @ParameterizedTest(name = "Should return 'Just started' status when \"{0}\" is triggered .")
  @ValueSource(strings = {
      "personPlacementTrainingBodyTrustJob",
      "personPlacementEmployingBodyTrustJob",
      "postEmployingBodyTrustJob",
      "postTrainingBodyTrustJob",
      "personElasticSearchSyncJob",
      "personOwnerRebuildJob",
      "personRecordStatusJob",
      "revalCurrentPmJob",
      "postFundingSyncJob"
  })
  void shouldReturnJustStartedWhenAJobTriggered(String name) throws Exception {
    when(personPlacementTrainingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(false);
    when(personPlacementEmployingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(false);
    when(postEmployingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(false);
    when(postTrainingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(false);
    when(personElasticSearchSyncJob.isCurrentlyRunning())
        .thenReturn(false);
    when(personOwnerRebuildJob.isCurrentlyRunning())
        .thenReturn(false);
    when(personRecordStatusJob.isCurrentlyRunning())
        .thenReturn(false);
    when(revalCurrentPmSyncJob.isCurrentlyRunning())
        .thenReturn(false);
    when(postFundingSyncJob.isCurrentlyRunning())
        .thenReturn(false);

    mockMvc.perform(put("/api/job/" + name)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Just started"));
  }

  @Test
  void shouldReturnErrorWhenRevalCurrentPMSyncIsTriggeredButNotAvailable() throws Exception {
    jobResource.setRevalCurrentPmSyncJob(null);

    mockMvc.perform(put("/api/job/revalCurrentPmJob")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Job not found"));
  }

  @Test
  void shouldReturnErrorWhenRevalCurrentPlacementSyncIsTriggeredButNotAvailable() throws Exception {
    jobResource.setRevalCurrentPlacementSyncJob(null);

    mockMvc.perform(put("/api/job/revalCurrentPlacementJob")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Job not found"));
  }

  @Test
  void shouldReturnErrorWhenPostFundingSyncJobIsTriggeredButNotAvailable() throws Exception {
    jobResource.setPostFundingSyncJob(null);

    mockMvc.perform(put("/api/job/postFundingSyncJob")
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Job not found"));
  }

  @DisplayName("run personRecordStatusJob with correct date argument")
  @ParameterizedTest(name = "Should return 'Just started' status when personRecordStatusJob is triggered with \"{0}\".")
  @ValueSource(strings = {
      "ANY",
      "NONE",
      "2022-01-01",
      ""
  })
  void shouldReturnJustStartedWhenPersonRecordStatusJobWithCorrectArg(String arg)
      throws Exception {
    when(personRecordStatusJob.isCurrentlyRunning())
        .thenReturn(false);

    final String jobParams = String.format("{\"dateOverride\":\"%s\"}", arg);
    mockMvc.perform(put("/api/job/personRecordStatusJob")
            .contentType(MediaType.APPLICATION_JSON)
            .content(jobParams))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Just started"));
    verify(personRecordStatusJob).run(jobParams);
  }

  @DisplayName("run personRecordStatusJob with incorrect date argument")
  @ParameterizedTest(name = "Should return error status when personRecordStatusJob is triggered with \"{0}\".")
  @ValueSource(strings = {
      "aaa",
      "01/01/2020",
      "2022-02-30",
  })
  void shouldReturnErrorWhenPersonRecordStatusJobWithInvalidArg(String arg)
      throws Exception {
    when(personRecordStatusJob.isCurrentlyRunning())
        .thenReturn(false);

    String requestParam = String.format("{date:\"%s\"}", arg);

    doThrow(new IllegalArgumentException(String.format("The date is not correct: %s", arg)))
        .when(personRecordStatusJob).run(requestParam);

    mockMvc.perform(put("/api/job/personRecordStatusJob")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestParam))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value(Matchers.containsString("The date is not correct")));
  }

  @DisplayName("run an already running job")
  @ParameterizedTest(name = "Should return 'already running' status when trigger a running job \"{0}\".")
  @ValueSource(strings = {
      "personPlacementTrainingBodyTrustJob",
      "personPlacementEmployingBodyTrustJob",
      "postEmployingBodyTrustJob",
      "postTrainingBodyTrustJob",
      "personElasticSearchSyncJob",
      "personOwnerRebuildJob",
      "personRecordStatusJob",
      "revalCurrentPmJob",
      "postFundingSyncJob"
  })
  void shouldReturnAlreadyRunningWhenTriggerARunningJob(String name) throws Exception {

    when(personPlacementTrainingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(true);
    when(personPlacementEmployingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(true);
    when(postEmployingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(true);
    when(postTrainingBodyTrustJob.isCurrentlyRunning())
        .thenReturn(true);
    when(personElasticSearchSyncJob.isCurrentlyRunning())
        .thenReturn(true);
    when(personOwnerRebuildJob.isCurrentlyRunning())
        .thenReturn(true);
    when(personRecordStatusJob.isCurrentlyRunning())
        .thenReturn(true);
    when(revalCurrentPmSyncJob.isCurrentlyRunning())
        .thenReturn(true);
    when(postFundingSyncJob.isCurrentlyRunning())
        .thenReturn(true);

    mockMvc.perform(put("/api/job/" + name)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("Already running"));
  }

  @DisplayName("run a nonexistent job")
  @Test
  void shouldReturnBadRequestWhenTriggeringANonexistentJob() throws Exception {
    mockMvc.perform(put("/api/job/" + "nonexistentJob")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Job not found"));
  }

  @Test
  void shouldGetSysProfile() throws Exception {
    ReflectionTestUtils.setField(jobResource, "activeProfile", "prod,nimdta");
    mockMvc.perform(get("/api/sys/profile")
            .contentType(MediaType.TEXT_PLAIN))
        .andExpect(status().isOk())
        .andExpect(content().string("prod,nimdta"));
  }

  @Test
  void shouldRunAll() throws Exception {
    final JobRunningListener mockJobListener = mock(JobRunningListener.class);
    ReflectionTestUtils.setField(jobResource, "jobRunningListener", mockJobListener);
    mockMvc.perform(put("/api/jobs"))
        .andExpect(status().isOk());
    verify(mockJobListener).runJobs();
  }
}
