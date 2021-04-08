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
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

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

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Post");
      put("id", "10");
    }};
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenPostNotFound() {
    when(tcsService.getPostById(10L))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Post");
      put("id", "10");
    }};
    Object post = service.retrieveDto(message);

    assertThat("Unexpected DTO.", post, nullValue());
  }

  @Test
  void shouldReturnTrustWhenTrustFound() {
    TrustDTO expectedDto = new TrustDTO();
    when(referenceService.findTrustById(20L)).thenReturn(expectedDto);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Trust");
      put("id", "20");
    }};
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenTrustNotFound() {
    when(referenceService.findTrustById(20L))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Trust");
      put("id", "20");
    }};
    Object trust = service.retrieveDto(message);

    assertThat("Unexpected DTO.", trust, nullValue());
  }

  @Test
  void shouldReturnSiteWhenSiteFound() {
    SiteDTO expectedDto = new SiteDTO();
    when(referenceService.findSitesIdIn(Collections.singleton(30L)))
        .thenReturn(Collections.singletonList(expectedDto));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Site");
      put("id", "30");
    }};
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenSiteNotFound() {
    when(referenceService.findSitesIdIn(Collections.singleton(30L)))
        .thenReturn(Collections.emptyList());

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Site");
      put("id", "30");
    }};
    Object site = service.retrieveDto(message);

    assertThat("Unexpected DTO.", site, nullValue());
  }

  @Test
  void shouldReturnNullWhenFindSiteThrowsException() {
    when(referenceService.findSitesIdIn(Collections.singleton(30L)))
        .thenThrow(new RuntimeException("Expected exception."));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Site");
      put("id", "30");
    }};
    Object site = service.retrieveDto(message);

    assertThat("Unexpected DTO.", site, nullValue());
  }

  @Test
  void shouldReturnProgrammeWhenProgrammeFound() {
    ProgrammeDTO expectedDto = new ProgrammeDTO();
    when(tcsService.findProgrammesIn(Collections.singletonList("40")))
        .thenReturn(Collections.singletonList(expectedDto));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Programme");
      put("id", "40");
    }};
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenProgrammeNotFound() {
    when(tcsService.findProgrammesIn(Collections.singletonList("40")))
        .thenReturn(null);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Programme");
      put("id", "40");
    }};
    Object programme = service.retrieveDto(message);

    assertThat("Unexpected DTO.", programme, nullValue());
  }

  @Test
  void shouldReturnNullWhenGetProgrammeByIdThrowsException() {
    when(tcsService.findProgrammesIn(Collections.singletonList("40")))
        .thenThrow(new RuntimeException("Expected exception."));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Programme");
      put("id", "40");
    }};
    Object programme = service.retrieveDto(message);

    assertThat("Unexpected DTO.", programme, nullValue());
  }

  @Test
  void shouldReturnCurriculumWhenCurriculumFound() {
    CurriculumDTO expectedDto = new CurriculumDTO();
    when(tcsService.getCurriculumById(50L))
        .thenReturn(expectedDto);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Curriculum");
      put("id", "50");
    }};
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenCurriculumNotFound() {
    when(tcsService.getCurriculumById(50L))
        .thenReturn(null);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Curriculum");
      put("id", "50");
    }};
    Object curriculum = service.retrieveDto(message);

    assertThat("Unexpected DTO.", curriculum, nullValue());
  }

  @Test
  void shouldReturnNullWhenGetCurriculumByIdThrowsException() {
    when(tcsService.getCurriculumById(50L))
        .thenThrow(new RuntimeException("Expected exception."));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Curriculum");
      put("id", "50");
    }};
    Object curriculum = service.retrieveDto(message);

    assertThat("Unexpected DTO.", curriculum, nullValue());
  }

  @Test
  void shouldReturnSpecialtyWhenSpecialtyFound() {
    SpecialtyDTO expectedDto = new SpecialtyDTO();
    when(tcsService.getSpecialtyById(60L))
        .thenReturn(expectedDto);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Specialty");
      put("id", "60");
    }};
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedDto));
  }

  @Test
  void shouldReturnNullWhenSpecialtyNotFound() {
    when(tcsService.getSpecialtyById(60L))
        .thenReturn(null);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Specialty");
      put("id", "60");
    }};
    Object specialty = service.retrieveDto(message);

    assertThat("Unexpected DTO.", specialty, nullValue());
  }

  @Test
  void shouldReturnNullWhenGetSpecialtyByIdThrowsException() {
    when(tcsService.getSpecialtyById(60L))
        .thenThrow(new RuntimeException("Expected exception."));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Specialty");
      put("id", "60");
    }};
    Object specialty = service.retrieveDto(message);

    assertThat("Unexpected DTO.", specialty, nullValue());
  }

  @Test
  void shouldReturnPlacementSpecialtyWhenPrimarySpecialtyFound() {
    PlacementSpecialtyDTO expectedPrimaryDto = new PlacementSpecialtyDTO();
    expectedPrimaryDto.setPlacementSpecialtyType(PostSpecialtyType.PRIMARY);

    PlacementDetailsDTO expectedPlacementDetailsDto = new PlacementDetailsDTO();
    expectedPlacementDetailsDto
        .setSpecialties(new HashSet<>(Collections.singletonList(expectedPrimaryDto)));
    when(tcsService.getPlacementById(70L))
        .thenReturn(expectedPlacementDetailsDto);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "PlacementSpecialty");
      put("placementId", "70");
      put("placementSpecialtyType", "PRIMARY");
    }};
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, sameInstance(expectedPrimaryDto));
  }

  @Test
  void shouldReturnNullWhenOnlyNonPrimaryPlacementSpecialtyFound() {
    PlacementSpecialtyDTO expectedNonPrimaryDto = new PlacementSpecialtyDTO();
    expectedNonPrimaryDto.setPlacementSpecialtyType(PostSpecialtyType.SUB_SPECIALTY);

    PlacementDetailsDTO expectedPlacementDetailsDto = new PlacementDetailsDTO();
    expectedPlacementDetailsDto
        .setSpecialties(new HashSet<>(Collections.singletonList(expectedNonPrimaryDto)));
    when(tcsService.getPlacementById(70L))
        .thenReturn(expectedPlacementDetailsDto);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "PlacementSpecialty");
      put("placementId", "70");
      put("placementSpecialtyType", "PRIMARY");
    }};
    Object retrievedDto = service.retrieveDto(message);

    assertThat("Unexpected DTO.", retrievedDto, nullValue());
  }

  @Test
  void shouldReturnNullWhenPlacementSpecialtyNotFound() {
    PlacementDetailsDTO expectedPlacementDetailsDto = new PlacementDetailsDTO();
    expectedPlacementDetailsDto.setSpecialties(new HashSet<>());
    when(tcsService.getPlacementById(70L))
        .thenReturn(expectedPlacementDetailsDto);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "PlacementSpecialty");
      put("placementId", "70");
      put("placementSpecialtyType", "PRIMARY");
    }};
    Object placementSpecialty = service.retrieveDto(message);

    assertThat("Unexpected DTO.", placementSpecialty, nullValue());
  }

  @Test
  void shouldReturnNullWhenGetPlacementByIdThrowsException() {
    when(tcsService.getPlacementById(70L))
        .thenThrow(new RuntimeException("Expected exception."));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "PlacementSpecialty");
      put("placementId", "70");
      put("placementSpecialtyType", "PRIMARY");
    }};
    Object placementSpecialty = service.retrieveDto(message);

    assertThat("Unexpected DTO.", placementSpecialty, nullValue());
  }

  @Test
  void shouldReturnNullWhenTableDoesNotMatchAnyCase() {
    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Wrong");
      put("id", "0");
    }};
    assertThat(service.retrieveDto(message), nullValue());
  }
}
