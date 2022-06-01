package uk.nhs.tis.sync.service;

import static com.transformuk.hee.tis.reference.api.enums.Status.CURRENT;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.ContactDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonalDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyGroupDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.AssessmentType;
import com.transformuk.hee.tis.tcs.api.enumeration.CurriculumSubType;
import com.transformuk.hee.tis.tcs.api.enumeration.LifecycleState;
import com.transformuk.hee.tis.tcs.api.enumeration.PlacementStatus;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.nhs.tis.sync.dto.CurriculumDmsDto;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.dto.PlacementDetailsDmsDto;
import uk.nhs.tis.sync.dto.PostDmsDto;
import uk.nhs.tis.sync.dto.ProgrammeDmsDto;
import uk.nhs.tis.sync.dto.SpecialtyDmsDto;

class DmsRecordAssemblerTest {

  private DmsRecordAssembler dmsRecordAssembler;

  @BeforeEach
  void setUp() {
    dmsRecordAssembler = new DmsRecordAssembler();
  }

  @Test
  void shouldAssembleMultipleDtos() {
    PostDTO post = new PostDTO();
    post.setId(1L);

    ProgrammeDTO programme = new ProgrammeDTO();
    programme.setId(2L);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        Arrays.asList(post, new Object(), programme));

    assertThat("Unexpected DMS DTO count.", dmsDtos.size(), is(2));

    DmsDto dmsDto = dmsDtos.get(0);
    assertThat("Unexpected DMS DTO type.", dmsDto.getData(), instanceOf(PostDmsDto.class));
    PostDmsDto postDmsDto = (PostDmsDto) dmsDto.getData();
    assertThat("Unexpected post ID.", postDmsDto.getId(), is("1"));

    dmsDto = dmsDtos.get(1);
    assertThat("Unexpected DMS DTO type.", dmsDto.getData(), instanceOf(ProgrammeDmsDto.class));
    ProgrammeDmsDto programmeDmsDto = (ProgrammeDmsDto) dmsDto.getData();
    assertThat("Unexpected programme ID.", programmeDmsDto.getId(), is("2"));
  }

  @Test
  void shouldAssembleContactDetails() {
    ContactDetailsDTO contactDetails = new ContactDetailsDTO();
    contactDetails.setId(10L);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(singletonList(contactDetails));

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);
    assertThat("Unexpected data.", dmsDto.getData(), sameInstance(contactDetails));

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key type.", metadata.getPartitionKeyType(),
        is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("ContactDetails"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());
  }

  @Test
  void shouldAssembleGdcDetails() {
    GdcDetailsDTO gdcDetails = new GdcDetailsDTO();
    gdcDetails.setId(10L);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(singletonList(gdcDetails));

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);
    assertThat("Unexpected data.", dmsDto.getData(), sameInstance(gdcDetails));

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key type.", metadata.getPartitionKeyType(),
        is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("GdcDetails"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());
  }

  @Test
  void shouldAssembleGmcDetails() {
    GmcDetailsDTO gmcDetails = new GmcDetailsDTO();
    gmcDetails.setId(10L);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(singletonList(gmcDetails));

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);
    assertThat("Unexpected data.", dmsDto.getData(), sameInstance(gmcDetails));

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key type.", metadata.getPartitionKeyType(),
        is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("GmcDetails"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());
  }

  @Test
  @Disabled("Not yet implemented")
  void shouldAssemblePerson() {
    Assertions.fail("Not yet implemented.");
  }

  @Test
  void shouldAssemblePersonalDetails() {
    PersonalDetailsDTO personalDetails = new PersonalDetailsDTO();
    personalDetails.setId(10L);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(singletonList(personalDetails));

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);
    assertThat("Unexpected data.", dmsDto.getData(), sameInstance(personalDetails));

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key type.", metadata.getPartitionKeyType(),
        is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("PersonalDetails"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());
  }

  @Test
  void shouldAssembleADmsDtoWhenGivenAPostDto() {
    PostDTO newPost = new PostDTO();
    newPost.setId(184668L);

    PostDTO postDto = new PostDTO();
    postDto.setId(44381L);
    postDto.setNationalPostNumber("EAN/8EJ83/094/SPR/001");
    postDto.status(Status.CURRENT);
    postDto.employingBodyId(287L);
    postDto.trainingBodyId(1464L);
    postDto.newPost(newPost);
    postDto.oldPost(null);
    postDto.owner("Health Education England North West London");
    postDto.intrepidId("128374444");

    List<DmsDto> actualDmsDtos = dmsRecordAssembler.assembleDmsDtos(singletonList(postDto));

    assertThat("Unexpected DTO count.", actualDmsDtos.size(), is(1));
    DmsDto actualDmsDto = actualDmsDtos.get(0);

    PostDmsDto expectedPostDmsDto = new PostDmsDto();
    expectedPostDmsDto.setId("44381");
    expectedPostDmsDto.setNationalPostNumber("EAN/8EJ83/094/SPR/001");
    expectedPostDmsDto.setStatus("CURRENT");
    expectedPostDmsDto.setEmployingBodyId("287");
    expectedPostDmsDto.setTrainingBodyId("1464");
    expectedPostDmsDto.setOldPostId(null);
    expectedPostDmsDto.setNewPostId("184668");
    expectedPostDmsDto.setOwner("Health Education England North West London");
    expectedPostDmsDto.setIntrepidId("128374444");

    //inject the timestamp from the actualDmsDto into the expectedDmsDto
    String timestamp = actualDmsDto.getMetadata().getTimestamp();

    //inject the transaction-id from the actualDmsDto into the expectedDmsDto
    String transactionId = actualDmsDto.getMetadata().getTransactionId();

    MetadataDto expectedMetadataDto = new MetadataDto();
    expectedMetadataDto.setTimestamp(timestamp);
    expectedMetadataDto.setRecordType("data");
    expectedMetadataDto.setOperation("load");
    expectedMetadataDto.setPartitionKeyType("schema-table");
    expectedMetadataDto.setSchemaName("tcs");
    expectedMetadataDto.setTableName("Post");
    expectedMetadataDto.setTransactionId(transactionId);

    DmsDto expectedDmsDto = new DmsDto(expectedPostDmsDto, expectedMetadataDto);

    assertEquals(expectedDmsDto, actualDmsDto);
  }

  @Test
  @Disabled("Not yet implemented")
  void shouldAssembleProgrammeMembership() {
    Assertions.fail("Not yet implemented.");
  }

  @Test
  @Disabled("Not yet implemented")
  void shouldAssembleQualification() {
    Assertions.fail("Not yet implemented.");
  }

  @Test
  void shouldAssembleADmsDtoWhenGivenATrustDto() {
    TrustDTO trustDto = new TrustDTO();
    trustDto.setId(333L);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(singletonList(trustDto));

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);
    assertThat("Unexpected data.", dmsDto.getData(), sameInstance(trustDto));

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key type.", metadata.getPartitionKeyType(),
        is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("reference"));
    assertThat("Unexpected table.", metadata.getTableName(), is("Trust"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());
  }

  @Test
  void shouldHandleSite() {
    SiteDTO site = new SiteDTO();
    site.setId(40L);
    site.setStatus(CURRENT);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(singletonList(site));

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);
    assertThat("Unexpected data.", dmsDto.getData(), sameInstance(site));

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key type.", metadata.getPartitionKeyType(),
        is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("reference"));
    assertThat("Unexpected table.", metadata.getTableName(), is("Site"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());
  }

  @Test
  void shouldReturnEmptyWhenUnsupportedType() {
    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(singletonList(new Object()));
    assertThat("Unexpected dms dto count.", dmsDtos.size(), is(0));
  }

  @Test
  void shouldHandleProgramme() {
    ProgrammeDTO programme = new ProgrammeDTO();
    programme.setId(50L);
    programme.setOwner("owner");
    programme.setIntrepidId("i50");
    programme.setProgrammeName("programmeName");
    programme.setProgrammeNumber("500");
    programme.setStatus(Status.CURRENT);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(singletonList(programme));

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected record type.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key.", metadata.getPartitionKeyType(), is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("Programme"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());

    Object data = dmsDto.getData();
    assertThat("Unexpected data.", data, instanceOf(ProgrammeDmsDto.class));

    ProgrammeDmsDto programmeDmsDto = (ProgrammeDmsDto) data;
    assertThat("Unexpected record type.", programmeDmsDto.getId(), is("50"));
    assertThat("Unexpected record type.", programmeDmsDto.getIntrepidId(), is("i50"));
    assertThat("Unexpected record type.", programmeDmsDto.getOwner(), is("owner"));
    assertThat("Unexpected record type.", programmeDmsDto.getProgrammeName(), is("programmeName"));
    assertThat("Unexpected record type.", programmeDmsDto.getProgrammeNumber(), is(
        "500"));
    assertThat("Unexpected record type.", programmeDmsDto.getStatus(), is("CURRENT"));
  }

  @Test
  void shouldHandleCurriculum() {
    CurriculumDTO curriculum = new CurriculumDTO();
    curriculum.setId(60L);
    curriculum.setName("name");
    curriculum.setCurriculumSubType(CurriculumSubType.DENTAL_CURRICULUM);
    curriculum.setAssessmentType(AssessmentType.ACADEMIC);
    curriculum.setDoesThisCurriculumLeadToCct(true);
    curriculum.setPeriodOfGrace(10);
    curriculum.setIntrepidId("i60");

    SpecialtyDTO specialty = new SpecialtyDTO();
    specialty.setId(2L);
    curriculum.setSpecialty(specialty);

    curriculum.setStatus(Status.CURRENT);
    curriculum.setLength(12);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(singletonList(curriculum));

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key.", metadata.getPartitionKeyType(), is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("Curriculum"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());

    Object data = dmsDto.getData();
    assertThat("Unexpected data.", data, instanceOf(CurriculumDmsDto.class));

    CurriculumDmsDto curriculumDmsDto = (CurriculumDmsDto) data;
    assertThat("Unexpected id.", curriculumDmsDto.getId(), is("60"));
    assertThat("Unexpected name.", curriculumDmsDto.getName(), is("name"));
    assertThat("Unexpected curriculumSubType.", curriculumDmsDto.getCurriculumSubType(),
        is(CurriculumSubType.DENTAL_CURRICULUM.name()));
    assertThat("Unexpected assessmentType.", curriculumDmsDto.getAssessmentType(),
        is(AssessmentType.ACADEMIC.name()));
    assertThat("Unexpected doesThisCurriculumLeadToCct.",
        curriculumDmsDto.getDoesThisCurriculumLeadToCct(),
        is("true"));
    assertThat("Unexpected periodOfGrace.", curriculumDmsDto.getPeriodOfGrace(), is("10"));
    assertThat("Unexpected intrepidId.", curriculumDmsDto.getIntrepidId(), is("i60"));
    assertThat("Unexpected specialtyId.", curriculumDmsDto.getSpecialtyId(), is("2"));
    assertThat("Unexpected status.", curriculumDmsDto.getStatus(), is("CURRENT"));
    assertThat("Unexpected length.", curriculumDmsDto.getLength(), is("12"));
  }

  @Test
  void shouldHandleSpecialty() {
    SpecialtyDTO specialty = new SpecialtyDTO();
    specialty.setId(70L);
    specialty.setCollege("college");
    specialty.setIntrepidId("i70");
    specialty.setName("specialtyName");
    specialty.setSpecialtyCode("specialtyCode");
    specialty.setStatus(Status.CURRENT);

    SpecialtyGroupDTO specialtyGroupDto = new SpecialtyGroupDTO();
    specialtyGroupDto.setId(75L);
    specialty.setSpecialtyGroup(specialtyGroupDto);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(singletonList(specialty));

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key.", metadata.getPartitionKeyType(), is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("Specialty"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());

    Object data = dmsDto.getData();
    assertThat("Unexpected data.", data, instanceOf(SpecialtyDmsDto.class));

    SpecialtyDmsDto specialtyDmsDto = (SpecialtyDmsDto) data;
    assertThat("Unexpected id.", specialtyDmsDto.getId(), is("70"));
    assertThat("Unexpected intrepid id.", specialtyDmsDto.getIntrepidId(), is("i70"));
    assertThat("Unexpected specialty name.", specialtyDmsDto.getName(), is("specialtyName"));
    assertThat("Unexpected college.", specialtyDmsDto.getCollege(), is("college"));
    assertThat("Unexpected specialty code.", specialtyDmsDto.getSpecialtyCode(),
        is("specialtyCode"));
    assertThat("Unexpected status.", specialtyDmsDto.getStatus(), is("CURRENT"));
    assertThat("Unexpected specialty group id.", specialtyDmsDto.getSpecialtyGroupId(), is("75"));
  }

  @Test
  void shouldHandlePlacementSpecialty() {
    PlacementSpecialtyDTO placementSpecialty = new PlacementSpecialtyDTO();
    placementSpecialty.setPlacementId(80L);
    placementSpecialty.setSpecialtyId(90L);
    placementSpecialty.setPlacementSpecialtyType(PostSpecialtyType.PRIMARY);
    placementSpecialty.setSpecialtyName("specialtyName");

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(singletonList(placementSpecialty));

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);
    assertThat("Unexpected data.", dmsDto.getData(), sameInstance(placementSpecialty));

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key type.", metadata.getPartitionKeyType(),
        is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("PlacementSpecialty"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());
  }

  @Test
  void shouldHandlePlacement() {
    PlacementDetailsDTO placementDetailsDto = new PlacementDetailsDTO();
    placementDetailsDto.setId(45L);
    placementDetailsDto.setDateFrom(LocalDate.MIN);
    placementDetailsDto.setDateTo(LocalDate.MAX);
    placementDetailsDto.setWholeTimeEquivalent(new BigDecimal("1"));
    placementDetailsDto.setIntrepidId("00");
    placementDetailsDto.setTraineeId(4500L);
    placementDetailsDto.setPostId(5L);
    placementDetailsDto.setGradeAbbreviation("gradeAbbreviation");
    placementDetailsDto.setPlacementType("placementType");
    placementDetailsDto.setStatus(PlacementStatus.CURRENT);
    placementDetailsDto.setTrainingDescription("trainingDescription");
    placementDetailsDto.setGradeId(20L);
    placementDetailsDto.setLifecycleState(LifecycleState.APPROVED);
    placementDetailsDto.setSiteId(30L);
    placementDetailsDto.setSiteCode("siteCode");
    placementDetailsDto.setLocalPostNumber("PO5TN0");

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(singletonList(placementDetailsDto));

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key.", metadata.getPartitionKeyType(), is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("Placement"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());

    Object data = dmsDto.getData();
    assertThat("Unexpected data.", data, instanceOf(PlacementDetailsDmsDto.class));

    PlacementDetailsDmsDto placementDetailsDmsDto = (PlacementDetailsDmsDto) data;
    assertThat("Unexpected id", "45", is(placementDetailsDmsDto.getId()));
    assertThat("Unexpected dateFrom", LocalDate.MIN.toString(),
        is(placementDetailsDmsDto.getDateFrom()));
    assertThat("Unexpected dateTo", LocalDate.MAX.toString(),
        is(placementDetailsDmsDto.getDateTo()));
    assertThat("Unexpected wholeTimeEquivalent", "1",
        is(placementDetailsDmsDto.getWholeTimeEquivalent()));
    assertThat("Unexpected intrepidId", "00", is(placementDetailsDmsDto.getIntrepidId()));
    assertThat("Unexpected traineeId", "4500", is(placementDetailsDmsDto.getTraineeId()));
    assertThat("Unexpected postId", "5", is(placementDetailsDmsDto.getPostId()));
    assertThat("Unexpected gradeAbbreviation", "gradeAbbreviation",
        is(placementDetailsDmsDto.getGradeAbbreviation()));
    assertThat("Unexpected placementType", "placementType",
        is(placementDetailsDmsDto.getPlacementType()));
    assertThat("Unexpected status", "CURRENT", is(placementDetailsDmsDto.getStatus()));
    assertThat("Unexpected trainingDescription", "trainingDescription",
        is(placementDetailsDmsDto.getTrainingDescription()));
    assertThat("Unexpected gradeId", "20", is(placementDetailsDmsDto.getGradeId()));
    assertThat("Unexpected lifecycleState", "APPROVED",
        is(placementDetailsDmsDto.getLifecycleState()));
    assertThat("Unexpected siteId", "30", is(placementDetailsDmsDto.getSiteId()));
    assertThat("Unexpected siteCode", "siteCode", is(placementDetailsDmsDto.getSiteCode()));
    assertThat("Unexpected localPostNumber", "PO5TN0",
        is(placementDetailsDmsDto.getLocalPostNumber()));
  }
}
