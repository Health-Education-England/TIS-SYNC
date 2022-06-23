package uk.nhs.tis.sync.service;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.ContactDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonalDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSummaryDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.QualificationDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

class DataRequestServiceTest {

  private static final String FORENAMES = "Joe";
  private static final String SURNAME = "Bloggs";

  private static final String GDC_NUMBER = "gdc123";
  private static final String GMC_NUMBER = "gmc123";

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
  void shouldReturnFullSyncDataWhenPersonFound() {
    String personIdString = "10";
    long personId = 10;

    // Create person with one-to-one data mappings.
    PersonDTO person = new PersonDTO();
    person.setId(personId);

    ContactDetailsDTO contactDetails = new ContactDetailsDTO();
    contactDetails.setId(personId);
    contactDetails.setLegalForenames(FORENAMES);
    contactDetails.setSurname(SURNAME);
    person.setContactDetails(contactDetails);

    GdcDetailsDTO gdcDetails = new GdcDetailsDTO();
    gdcDetails.setId(personId);
    gdcDetails.setGdcNumber(GDC_NUMBER);
    person.setGdcDetails(gdcDetails);

    GmcDetailsDTO gmcDetails = new GmcDetailsDTO();
    gmcDetails.setId(personId);
    gmcDetails.setGmcNumber(GMC_NUMBER);
    person.setGmcDetails(gmcDetails);

    PersonalDetailsDTO personalDetails = new PersonalDetailsDTO();
    personalDetails.setId(personId);
    personalDetails.setDateOfBirth(LocalDate.now());
    person.setPersonalDetails(personalDetails);

    // Add programme memberships.
    Set<ProgrammeMembershipDTO> programmeMemberships = new HashSet<>();

    ProgrammeMembershipDTO programmeMembership1 = new ProgrammeMembershipDTO();
    programmeMembership1.setProgrammeId(30L);
    programmeMembership1.setPerson(person);
    programmeMembership1.setProgrammeName("programme one");
    programmeMemberships.add(programmeMembership1);

    ProgrammeMembershipDTO programmeMembership2 = new ProgrammeMembershipDTO();
    programmeMembership2.setProgrammeId(31L);
    programmeMembership2.setPerson(person);
    programmeMembership2.setProgrammeName("programme two");
    programmeMemberships.add(programmeMembership2);

    person.setProgrammeMemberships(programmeMemberships);

    // Add qualifications.
    Set<QualificationDTO> qualifications = new HashSet<>();

    QualificationDTO qualification1 = new QualificationDTO();
    qualification1.setId(40L);
    qualification1.setPerson(person);
    qualification1.setQualification("qualification one");
    qualifications.add(qualification1);

    QualificationDTO qualification2 = new QualificationDTO();
    qualification2.setId(41L);
    qualification2.setPerson(person);
    qualification2.setQualification("qualification two");
    qualifications.add(qualification2);

    person.setQualifications(qualifications);

    when(tcsService.getPerson(personIdString)).thenReturn(person);

    // Create placements
    List<PlacementSummaryDTO> placements = new ArrayList<>();

    PlacementSummaryDTO placement1 = new PlacementSummaryDTO();
    placement1.setPlacementId(20L);
    placement1.setTraineeId(personId);
    placements.add(placement1);

    PlacementSummaryDTO placement2 = new PlacementSummaryDTO();
    placement2.setPlacementId(21L);
    placement2.setTraineeId(personId);
    placements.add(placement2);

    when(tcsService.getPlacementForTrainee(personId)).thenReturn(placements);

    Map<String, String> message = new HashMap<>();
    message.put("table", "Person");
    message.put("id", personIdString);

    List<Object> dtos = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", dtos.size(), is(11));
    assertThat("Unexpected DTOs.", dtos, hasItems(contactDetails, gdcDetails, gmcDetails,
        person, personalDetails, placement1, placement2, programmeMembership1, programmeMembership2,
        qualification1, qualification2));
  }

  @Test
  void shouldReturnPostWhenPostFound() {
    PostDTO expectedDto = new PostDTO();
    when(tcsService.getPostById(10L)).thenReturn(expectedDto);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Post");
      put("id", "10");
    }};
    List<Object> retrievedDtos = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", retrievedDtos.size(), is(1));
    assertThat("Unexpected DTO.", retrievedDtos.get(0), sameInstance(expectedDto));
  }

  @Test
  void shouldReturnEmptyWhenPostNotFound() {
    when(tcsService.getPostById(10L))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Post");
      put("id", "10");
    }};
    List<Object> posts = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", posts.size(), is(0));
  }

  @Test
  void shouldReturnTrustWhenTrustFound() {
    TrustDTO expectedDto = new TrustDTO();
    when(referenceService.findTrustById(20L)).thenReturn(expectedDto);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Trust");
      put("id", "20");
    }};
    List<Object> retrievedDtos = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", retrievedDtos.size(), is(1));
    assertThat("Unexpected DTO.", retrievedDtos.get(0), sameInstance(expectedDto));
  }

  @Test
  void shouldReturnEmptyWhenTrustNotFound() {
    when(referenceService.findTrustById(20L))
        .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Trust");
      put("id", "20");
    }};
    List<Object> trusts = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", trusts.size(), is(0));
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
    List<Object> retrievedDtos = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", retrievedDtos.size(), is(1));
    assertThat("Unexpected DTO.", retrievedDtos.get(0), sameInstance(expectedDto));
  }

  @Test
  void shouldReturnEmptyWhenSiteNotFound() {
    when(referenceService.findSitesIdIn(Collections.singleton(30L)))
        .thenReturn(Collections.emptyList());

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Site");
      put("id", "30");
    }};
    List<Object> sites = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", sites.size(), is(0));
  }

  @Test
  void shouldReturnEmptyWhenFindSiteThrowsException() {
    when(referenceService.findSitesIdIn(Collections.singleton(30L)))
        .thenThrow(new RuntimeException("Expected exception."));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Site");
      put("id", "30");
    }};
    List<Object> sites = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", sites.size(), is(0));
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
    List<Object> retrievedDtos = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", retrievedDtos.size(), is(1));
    assertThat("Unexpected DTO.", retrievedDtos.get(0), sameInstance(expectedDto));
  }

  @Test
  void shouldReturnEmptyWhenProgrammeNotFound() {
    when(tcsService.findProgrammesIn(Collections.singletonList("40")))
        .thenReturn(null);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Programme");
      put("id", "40");
    }};
    List<Object> programmes = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", programmes.size(), is(0));
  }

  @Test
  void shouldReturnEmptyWhenGetProgrammeByIdThrowsException() {
    when(tcsService.findProgrammesIn(Collections.singletonList("40")))
        .thenThrow(new RuntimeException("Expected exception."));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Programme");
      put("id", "40");
    }};
    List<Object> programmes = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", programmes.size(), is(0));
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
    List<Object> retrievedDtos = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", retrievedDtos.size(), is(1));
    assertThat("Unexpected DTO.", retrievedDtos.get(0), sameInstance(expectedDto));
  }

  @Test
  void shouldReturnEmptyWhenCurriculumNotFound() {
    when(tcsService.getCurriculumById(50L))
        .thenReturn(null);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Curriculum");
      put("id", "50");
    }};
    List<Object> curriculums = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", curriculums.size(), is(0));
  }

  @Test
  void shouldReturnEmptyWhenGetCurriculumByIdThrowsException() {
    when(tcsService.getCurriculumById(50L))
        .thenThrow(new RuntimeException("Expected exception."));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Curriculum");
      put("id", "50");
    }};
    List<Object> curriculums = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", curriculums.size(), is(0));
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
    List<Object> retrievedDtos = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", retrievedDtos.size(), is(1));
    assertThat("Unexpected DTO.", retrievedDtos.get(0), sameInstance(expectedDto));
  }

  @Test
  void shouldReturnEmptyWhenSpecialtyNotFound() {
    when(tcsService.getSpecialtyById(60L))
        .thenReturn(null);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Specialty");
      put("id", "60");
    }};
    List<Object> specialties = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", specialties.size(), is(0));
  }

  @Test
  void shouldReturnEmptyWhenGetSpecialtyByIdThrowsException() {
    when(tcsService.getSpecialtyById(60L))
        .thenThrow(new RuntimeException("Expected exception."));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Specialty");
      put("id", "60");
    }};
    List<Object> specialties = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", specialties.size(), is(0));
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
    List<Object> retrievedDtos = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", retrievedDtos.size(), is(1));
    assertThat("Unexpected DTO.", retrievedDtos.get(0), sameInstance(expectedPrimaryDto));
  }

  @Test
  void shouldReturnEmptyWhenOnlyNonPrimaryPlacementSpecialtyFound() {
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
    List<Object> retrievedDtos = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", retrievedDtos.size(), is(0));
  }

  @Test
  void shouldReturnEmptyWhenPlacementSpecialtyNotFound() {
    PlacementDetailsDTO expectedPlacementDetailsDto = new PlacementDetailsDTO();
    expectedPlacementDetailsDto.setSpecialties(new HashSet<>());
    when(tcsService.getPlacementById(70L))
        .thenReturn(expectedPlacementDetailsDto);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "PlacementSpecialty");
      put("placementId", "70");
      put("placementSpecialtyType", "PRIMARY");
    }};
    List<Object> placementSpecialties = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", placementSpecialties.size(), is(0));
  }

  @Test
  void shouldReturnEmptyWhenGetPlacementByIdThrowsExceptionRequestingAPlacementSpecialty() {
    when(tcsService.getPlacementById(70L))
        .thenThrow(new RuntimeException("Expected exception."));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "PlacementSpecialty");
      put("placementId", "70");
      put("placementSpecialtyType", "PRIMARY");
    }};
    List<Object> placementSpecialties = service.retrieveDtos(message);

    verify(tcsService).getPlacementById(70L);
    assertThat("Unexpected DTO count.", placementSpecialties.size(), is(0));
  }

  @Test
  void shouldReturnEmptyWhenGetPlacementByIdThrowsExceptionRequestingAPlacement() {
    when(tcsService.getPlacementById(80L))
        .thenThrow(new RuntimeException("Expected exception."));

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Placement");
      put("id", "80");
    }};
    List<Object> placements = service.retrieveDtos(message);

    verify(tcsService).getPlacementById(80L);
    assertThat("Unexpected DTO count.", placements.size(), is(0));
  }

  @Test
  void shouldReturnPlacementWhenPlacementFound() {
    PlacementDetailsDTO expectedDto = new PlacementDetailsDTO();
    when(tcsService.getPlacementById(80L)).thenReturn(expectedDto);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Placement");
      put("id", "80");
    }};
    List<Object> retrievedDtos = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", retrievedDtos.size(), is(1));
    assertThat("Unexpected DTO.", retrievedDtos.get(0), sameInstance(expectedDto));
  }

  @Test
  void shouldReturnEmptyWhenPlacementNotFound() {
    when(tcsService.getPlacementById(80L)).thenReturn(null);

    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Placement");
      put("id", "80");
    }};
    List<Object> specialties = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", specialties.size(), is(0));
  }

  @Test
  void shouldReturnEmptyWhenTableDoesNotMatchAnyCase() {
    Map<String, String> message = new HashMap<String, String>() {{
      put("table", "Wrong");
      put("id", "0");
    }};

    List<Object> retrievedDtos = service.retrieveDtos(message);

    assertThat("Unexpected DTO count.", retrievedDtos.size(), is(0));
  }

  @Test
  void shouldReturnEmptyWhenMessageContainsWrongKey() {
    // Malformed message with no "placementId" key
    Map<String, String> messageForSpecialty = new HashMap<String, String>() {{
      put("table", "PlacementSpecialty");
      put("placementSpecialtyType", "PRIMARY");
    }};
    List<Object> placementSpecialties = service.retrieveDtos(messageForSpecialty);

    assertThat("Unexpected DTO count.", placementSpecialties.size(), is(0));

    // Malformed message with no "id" key
    Map<String, String> messageForPost = new HashMap<String, String>() {{
      put("table", "Post");
    }};
    List<Object> posts = service.retrieveDtos(messageForPost);

    assertThat("Unexpected DTO count.", posts.size(), is(0));

    // Malformed message with no "table" key
    Map<String, String> messageForTrust = new HashMap<String, String>() {{
      put("id", "4");
    }};
    List<Object> trusts = service.retrieveDtos(messageForTrust);

    assertThat("Unexpected DTO count.", trusts.size(), is(0));
  }
}
