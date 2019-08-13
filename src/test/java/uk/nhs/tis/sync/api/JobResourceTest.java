package uk.nhs.tis.sync.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.nhs.tis.sync.job.*;
import uk.nhs.tis.sync.job.person.PersonElasticSearchSyncJob;

import javax.ws.rs.core.MediaType;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@RunWith(SpringRunner.class)
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

  private JobResource jobResource;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    jobResource = new JobResource(personPlacementEmployingBodyTrustJob,
      personPlacementTrainingBodyTrustJob,
      postEmployingBodyTrustJob,
      postTrainingBodyTrustJob,
      personElasticSearchSyncJob,
      personOwnerRebuildJob);
    mockMvc = MockMvcBuilders.standaloneSetup(jobResource).build();
  }

  @Test
  public void shouldReturnAllStatusWhenGetStatus() throws Exception{
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

    mockMvc.perform(get("/api/jobs/status")
      .content(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.personPlacementTrainingBodyTrustJob").value(true))
      .andExpect(jsonPath("$.personPlacementEmployingBodyTrustJob").value(false))
      .andExpect(jsonPath("$.postEmployingBodyTrustJob").value(false))
      .andExpect(jsonPath("$.postTrainingBodyTrustJob").value(false))
      .andExpect(jsonPath("$.personElasticSearchSyncJob").value(false))
      .andExpect(jsonPath("$.personOwnerRebuildJob").value(false))
      .andExpect(status().isOk());
  }

  @Test
  public void shouldReturnCorrectStatusWhenTriggerOneIndividualJob() throws Exception {
    when(personElasticSearchSyncJob.isCurrentlyRunning())
      .thenReturn(false);
    mockMvc.perform(put("/api/job/personElasticSearchSyncJob")
      .content(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("just started"));

    when(personOwnerRebuildJob.isCurrentlyRunning())
      .thenReturn(true);
    mockMvc.perform(put("/api/job/personOwnerRebuildJob")
      .content(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value("already running"));
  }
}
