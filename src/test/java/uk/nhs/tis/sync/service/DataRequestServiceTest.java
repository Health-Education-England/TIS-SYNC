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
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Collections;
import java.util.HashSet;
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

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Post", "10", null, null);
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenPostNotFound() {
    when(tcsService.getPostById(10L))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Post", "10", null, null);
    Object post = service.retrieveDto(message);

    assertThat("Unexpected DTO.", post, nullValue());
  }

  @Test
  void shouldReturnTrustWhenTrustFound() {
    TrustDTO expectedDto = new TrustDTO();
    when(referenceService.findTrustById(20L)).thenReturn(expectedDto);

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Trust", "20", null, null);
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenTrustNotFound() {
    when(referenceService.findTrustById(20L))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Trust", "20", null, null);
    Object trust = service.retrieveDto(message);

    assertThat("Unexpected DTO.", trust, nullValue());
  }

  @Test
  void shouldReturnSiteWhenSiteFound() {
    SiteDTO expectedDto = new SiteDTO();
    when(referenceService.findSitesIdIn(Collections.singleton(30L)))
        .thenReturn(Collections.singletonList(expectedDto));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Site", "30", null, null);
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenSiteNotFound() {
    when(referenceService.findSitesIdIn(Collections.singleton(30L)))
        .thenReturn(Collections.emptyList());

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Site", "30", null, null);
    Object site = service.retrieveDto(message);

    assertThat("Unexpected DTO.", site, nullValue());
  }

  @Test
  void shouldReturnNullWhenFindSiteThrowsException() {
    when(referenceService.findSitesIdIn(Collections.singleton(30L)))
        .thenThrow(new RuntimeException("Expected exception."));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Site", "30", null, null);
    Object site = service.retrieveDto(message);

    assertThat("Unexpected DTO.", site, nullValue());
  }

  @Test
  void shouldReturnProgrammeWhenProgrammeFound() {
    ProgrammeDTO expectedDto = new ProgrammeDTO();
    when(tcsService.findProgrammesIn(Collections.singletonList("40")))
        .thenReturn(Collections.singletonList(expectedDto));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Programme", "40", null, null);
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenProgrammeNotFound() {
    when(tcsService.findProgrammesIn(Collections.singletonList("40")))
        .thenReturn(null);

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Programme", "40", null, null);
    Object programme = service.retrieveDto(message);

    assertThat("Unexpected DTO.", programme, nullValue());
  }

  @Test
  void shouldReturnNullWhenGetProgrammeByIdThrowsException() {
    when(tcsService.findProgrammesIn(Collections.singletonList("40")))
        .thenThrow(new RuntimeException("Expected exception."));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Programme", "40", null, null);
    Object programme = service.retrieveDto(message);

    assertThat("Unexpected DTO.", programme, nullValue());
  }

  @Test
  void shouldReturnCurriculumWhenCurriculumFound() {
    CurriculumDTO expectedDto = new CurriculumDTO();
    when(tcsService.getCurriculumById(50L))
        .thenReturn(expectedDto);

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Curriculum", "50", null, null);
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenCurriculumNotFound() {
    when(tcsService.getCurriculumById(50L))
        .thenReturn(null);

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Curriculum", "50", null, null);
    Object curriculum = service.retrieveDto(message);

    assertThat("Unexpected DTO.", curriculum, nullValue());
  }

  @Test
  void shouldReturnNullWhenGetCurriculumByIdThrowsException() {
    when(tcsService.getCurriculumById(50L))
        .thenThrow(new RuntimeException("Expected exception."));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Curriculum", "50", null, null);
    Object curriculum = service.retrieveDto(message);

    assertThat("Unexpected DTO.", curriculum, nullValue());
  }

  @Test
  void shouldReturnSpecialtyWhenSpecialtyFound() {
    SpecialtyDTO expectedDto = new SpecialtyDTO();
    when(tcsService.getSpecialtyById(60L))
        .thenReturn(expectedDto);

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Specialty", "60", null, null);
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenSpecialtyNotFound() {
    when(tcsService.getSpecialtyById(60L))
        .thenReturn(null);

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Specialty", "60", null, null);
    Object specialty = service.retrieveDto(message);

    assertThat("Unexpected DTO.", specialty, nullValue());
  }

  @Test
  void shouldReturnNullWhenGetSpecialtyByIdThrowsException() {
    when(tcsService.getSpecialtyById(60L))
        .thenThrow(new RuntimeException("Expected exception."));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Specialty", "60", null, null);
    Object specialty = service.retrieveDto(message);

    assertThat("Unexpected DTO.", specialty, nullValue());
  }

  @Test
  void shouldReturnPlacementSpecialtyWhenSpecialtyFound() {
    PlacementSpecialtyDTO expectedDto = new PlacementSpecialtyDTO();
    PlacementDetailsDTO expectedPlacementDetailsDto = new PlacementDetailsDTO();
    expectedPlacementDetailsDto
        .setSpecialties(new HashSet<>(Collections.singletonList(expectedDto)));
    when(tcsService.getPlacementById(70L))
        .thenReturn(expectedPlacementDetailsDto);

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("PlacementSpecialty", null, "70",
        "PRIMARY");
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenPlacementSpecialtyNotFound() {
    PlacementDetailsDTO expectedPlacementDetailsDto = new PlacementDetailsDTO();
    expectedPlacementDetailsDto.setSpecialties(new HashSet<>());
    when(tcsService.getPlacementById(70L))
        .thenReturn(expectedPlacementDetailsDto);

    AmazonSqsMessageDto message1 = new AmazonSqsMessageDto("PlacementSpecialty", null, "70",
        "PRIMARY");
    Object placementSpecialty = service.retrieveDto(message1);

    assertThat("Unexpected DTO.", placementSpecialty, nullValue());

    when(tcsService.getPlacementById(80L))
        .thenReturn(null);

    AmazonSqsMessageDto message2 = new AmazonSqsMessageDto("PlacementSpecialty", null, "80",
        "PRIMARY");
    Object placementSpecialty2 = service.retrieveDto(message2);

    assertThat("Unexpected DTO.", placementSpecialty2, nullValue());
  }

  @Test
  void shouldReturnNullWhenGetPlacementByIdThrowsException() {
    when(tcsService.getPlacementById(70L))
        .thenThrow(new RuntimeException("Expected exception."));

    AmazonSqsMessageDto message = new AmazonSqsMessageDto("PlacementSpecialty", null, "70",
        "PRIMARY");
    Object specialty = service.retrieveDto(message);

    assertThat("Unexpected DTO.", specialty, nullValue());
  }

  @Test
  void shouldReturnNullWhenTableDoesNotMatchAnyCase() {
    AmazonSqsMessageDto message = new AmazonSqsMessageDto("Wrong", "0", null, null);
    assertThat(service.retrieveDto(message), nullValue());
  }
}
