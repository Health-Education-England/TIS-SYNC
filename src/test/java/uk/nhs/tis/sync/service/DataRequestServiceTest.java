package uk.nhs.tis.sync.service;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.nhs.tis.sync.dto.AmazonSqsMessageDto;

class DataRequestServiceTest {

  private DataRequestService service;

  private TcsServiceImpl tcsService;

  private ReferenceServiceImpl referenceService;

  @BeforeEach
  void setUp() {
    tcsService = mock(TcsServiceImpl.class);
    referenceService = mock(ReferenceServiceImpl.class);
    service = new DataRequestService(tcsService, referenceService);
  }

  @Test
  void shouldReturnPostWhenPostFound() {
    PostDTO expectedDto = new PostDTO();
    when(tcsService.getPostById(10L)).thenReturn(expectedDto);

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Post", "10");
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenPostNotFound() {
    when(tcsService.getPostById(10L))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Post", "10");
    Object post = service.retrieveDto(message);

    assertThat("Unexpected DTO.", post, nullValue());
  }

  @Test
  void shouldReturnTrustWhenTrustFound() {
    TrustDTO expectedDto = new TrustDTO();
    when(referenceService.findTrustById(20L)).thenReturn(expectedDto);

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Trust", "20");
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenTrustNotFound() {
    when(referenceService.findTrustById(20L))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Trust", "20");
    Object trust = service.retrieveDto(message);

    assertThat("Unexpected DTO.", trust, nullValue());
  }

  @Test
  void shouldReturnSiteWhenSiteFound() {
    SiteDTO expectedDto = new SiteDTO();
    when(referenceService.findSitesIdIn(Collections.singleton(30L)))
        .thenReturn(Collections.singletonList(expectedDto));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Site", "30");
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenSiteNotFound() {
    when(referenceService.findSitesIdIn(Collections.singleton(30L)))
        .thenReturn(Collections.emptyList());

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Site", "30");
    Object site = service.retrieveDto(message);

    assertThat("Unexpected DTO.", site, nullValue());
  }

  @Test
  void shouldReturnNullWhenFindSiteThrowsException() {
    when(referenceService.findSitesIdIn(Collections.singleton(30L)))
        .thenThrow(new RuntimeException("Expected exception."));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Site", "30");
    Object site = service.retrieveDto(message);

    assertThat("Unexpected DTO.", site, nullValue());
  }

  @Test
  void shouldReturnProgrammeWhenProgrammeFound() {
    ProgrammeDTO expectedDto = new ProgrammeDTO();
    when(tcsService.findProgrammesIn(Collections.singletonList("40")))
        .thenReturn(Collections.singletonList(expectedDto));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Programme", "40");
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenProgrammeNotFound() {
    when(tcsService.findProgrammesIn(Collections.singletonList("40")))
        .thenReturn(null);

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Programme", "40");
    Object programme = service.retrieveDto(message);

    assertThat("Unexpected DTO.", programme, nullValue());
  }

  @Test
  void shouldReturnNullWhenGetProgrammeByIdThrowsException() {
    when(tcsService.findProgrammesIn(Collections.singletonList("40")))
        .thenThrow(new RuntimeException("Expected exception."));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Programme", "40");
    Object programme = service.retrieveDto(message);

    assertThat("Unexpected DTO.", programme, nullValue());
  }

  @Test
  void shouldReturnCurriculumWhenCurriculumFound() {
    CurriculumDTO expectedDto = new CurriculumDTO();
    when(tcsService.getCurriculumById(50L))
        .thenReturn(expectedDto);

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Curriculum", "50");
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenCurriculumNotFound() {
    when(tcsService.getCurriculumById(50L))
        .thenReturn(null);

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Curriculum", "50");
    Object curriculum = service.retrieveDto(message);

    assertThat("Unexpected DTO.", curriculum, nullValue());
  }

  @Test
  void shouldReturnNullWhenGetCurriculumByIdThrowsException() {
    when(tcsService.getCurriculumById(50L))
        .thenThrow(new RuntimeException("Expected exception."));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Curriculum", "50");
    Object curriculum = service.retrieveDto(message);

    assertThat("Unexpected DTO.", curriculum, nullValue());
  }

  @Test
  void shouldReturnNullWhenTableDoesNotMatchAnyCase() {
    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Wrong", "0");
    assertThat(service.retrieveDto(message), nullValue());
  }
}
