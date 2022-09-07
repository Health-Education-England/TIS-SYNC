package uk.nhs.tis.sync.api;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.nhs.tis.sync.job.*;
import uk.nhs.tis.sync.job.person.PersonElasticSearchSyncJob;

import javax.ws.rs.core.MediaType;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@DisplayName("JobResource")
@ExtendWith(SpringExtension.class)
public class JobResourceTest {

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

  private JobResource jobResource;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    jobResource = new JobResource(personPlacementEmployingBodyTrustJob,
      personPlacementTrainingBodyTrustJob,
      postEmployingBodyTrustJob,
      postTrainingBodyTrustJob,
      personElasticSearchSyncJob,
      personOwnerRebuildJob,
      personRecordStatusJob);
    mockMvc = MockMvcBuilders.standaloneSetup(jobResource).build();
  }

  @DisplayName("get Status")
  @Test
  public void shouldReturnAllStatusWhenGetStatus() throws Exception {
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
      .andExpect(jsonPath("$.personPlacementTrainingBodyTrustJob").value(true))
      .andExpect(jsonPath("$.personPlacementEmployingBodyTrustJob").value(false))
      .andExpect(jsonPath("$.postEmployingBodyTrustJob").value(false))
      .andExpect(jsonPath("$.postTrainingBodyTrustJob").value(false))
      .andExpect(jsonPath("$.personElasticSearchSyncJob").value(false))
      .andExpect(jsonPath("$.personOwnerRebuildJob").value(false))
      .andExpect(jsonPath("$.personRecordStatusJob").value(false))
      .andExpect(status().isOk());
  }

  @DisplayName("run a job")
  @ParameterizedTest(name = "Should return 'just started' status when \"{0}\" is triggered .")
  @ValueSource(strings = {
    "personPlacementTrainingBodyTrustJob",
    "personPlacementEmployingBodyTrustJob",
    "postEmployingBodyTrustJob",
    "postTrainingBodyTrustJob",
    "personElasticSearchSyncJob",
    "personOwnerRebuildJob",
    "personRecordStatusJob"
  })
  public void shouldReturnJustStartedWhenAJobTriggered(String name) throws Exception {
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

    mockMvc.perform(put("/api/job/" + name)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("just started"));
  }

  @DisplayName("run personRecordStatusJob with date argument")
  @ParameterizedTest(name = "Should return 'just started' status when personRecordStatusJob is triggered with \"{0}\".")
  @ValueSource(strings = {
      "ANY",
      "AWS",
      "2022-01-01",
      ""
  })
  public void shouldReturnJustStartedWhenPersonRecordStatusJobWithCorrectArg(String arg)
      throws Exception{
    when(personRecordStatusJob.isCurrentlyRunning())
        .thenReturn(false);

    mockMvc.perform(put("/api/job/personRecordStatusJob")
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format("{date:\"%s\"}", arg)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("just started"));
  }

  @DisplayName("run personRecordStatusJob with date argument")
  @ParameterizedTest(name = "Should return error status when personRecordStatusJob is triggered with \"{0}\".")
  @ValueSource(strings = {
      "aaa",
      "01/01/2020",
      "2022-02-30",
  })
  public void shouldReturnErrorWhenPersonRecordStatusJobWithCorrectArg(String arg)
      throws Exception{
    when(personRecordStatusJob.isCurrentlyRunning())
        .thenReturn(false);

    String requestParam = String.format("{date:\"%s\"}", arg);

    doThrow(new IllegalArgumentException(String.format("The date is not correct: %s", arg)))
        .when(personRecordStatusJob).personRecordStatusJob(requestParam);

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
    "personRecordStatusJob"
  })
  public void shouldReturnAlreadyRunningWhenTriggerARunningJob(String name) throws Exception {
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

    mockMvc.perform(put("/api/job/" + name)
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("already running"));
  }

  @DisplayName("run a nonexistent job")
  @Test
  public void shouldReturnBadRequestWhenTriggeringANonexistentJob() throws Exception {
    mockMvc.perform(put("/api/job/" + "nonexistentJob")
        .contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.error").value("job not found"));
  }
}
