package uk.nhs.tis.sync.service;

import static com.transformuk.hee.tis.reference.api.enums.Status.CURRENT;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.ContactDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.GdcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.PersonalDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSummaryDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.QualificationDTO;
import com.transformuk.hee.tis.tcs.api.dto.RotationDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyGroupDTO;
import com.transformuk.hee.tis.tcs.api.dto.TrainingNumberDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.AssessmentType;
import com.transformuk.hee.tis.tcs.api.enumeration.CurriculumSubType;
import com.transformuk.hee.tis.tcs.api.enumeration.PlacementStatus;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import com.transformuk.hee.tis.tcs.api.enumeration.QualificationType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import uk.nhs.tis.sync.dto.CurriculumDmsDto;
import uk.nhs.tis.sync.dto.CurriculumMembershipDmsDto;
import uk.nhs.tis.sync.dto.CurriculumMembershipWrapperDto;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.GradeDmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.dto.PersonDmsDto;
import uk.nhs.tis.sync.dto.PlacementSpecialtyDmsDto;
import uk.nhs.tis.sync.dto.PlacementSummaryDmsDto;
import uk.nhs.tis.sync.dto.PostDmsDto;
import uk.nhs.tis.sync.dto.ProgrammeDmsDto;
import uk.nhs.tis.sync.dto.ProgrammeMembershipDmsDto;
import uk.nhs.tis.sync.dto.QualificationDmsDto;
import uk.nhs.tis.sync.dto.SiteDmsDto;
import uk.nhs.tis.sync.dto.SpecialtyDmsDto;
import uk.nhs.tis.sync.dto.TrustDmsDto;

class DmsRecordAssemblerTest {
  private static final String TIS_TRIGGER = "trigger";
  private static final String TIS_TRIGGER_DETAIL = "details";

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
        Arrays.asList(post, new Object(), programme), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

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

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(contactDetails), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

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

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(gdcDetails), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

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

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(gmcDetails), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

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
  void shouldAssemblePerson() {
    PersonDTO person = new PersonDTO();
    person.setId(10L);
    person.setIntrepidId("20");
    person.setAddedDate(LocalDateTime.MIN);
    person.setAmendedDate(LocalDateTime.MAX);
    person.setRole("role1,role2");
    person.setStatus(Status.INACTIVE);
    person.setComments("someComments");
    LocalDateTime now = LocalDateTime.now();
    person.setInactiveDate(now);
    person.setPublicHealthNumber("ph123");
    person.setRegulator("regulator");

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(person), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);
    assertThat("Unexpected data type.", dmsDto.getData(), instanceOf(PersonDmsDto.class));

    PersonDmsDto personDms = (PersonDmsDto) dmsDto.getData();
    assertThat("Unexpected id.", personDms.getId(), is("10"));
    assertThat("Unexpected intrepid id.", personDms.getIntrepidId(), is("20"));
    assertThat("Unexpected added date.", personDms.getAddedDate(),
        is(LocalDateTime.MIN.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    assertThat("Unexpected amended date.", personDms.getAmendedDate(),
        is(LocalDateTime.MAX.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    assertThat("Unexpected role.", personDms.getRole(), is("role1,role2"));
    assertThat("Unexpected status.", personDms.getStatus(), is("INACTIVE"));
    assertThat("Unexpected comments.", personDms.getComments(), is("someComments"));
    assertThat("Unexpected inactive date.", personDms.getInactiveDate(),
        is(now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    assertThat("Unexpected Public Health Number.", personDms.getPublicHealthNumber(), is("ph123"));
    assertThat("Unexpected regulator.", personDms.getRegulator(), is("regulator"));

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key type.", metadata.getPartitionKeyType(),
        is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("Person"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());
  }

  @Test
  void shouldAssemblePersonalDetails() {
    PersonalDetailsDTO personalDetails = new PersonalDetailsDTO();
    personalDetails.setId(10L);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(personalDetails), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

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

    List<DmsDto> actualDmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(postDto), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

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
    expectedMetadataDto.setTisTrigger(TIS_TRIGGER);
    expectedMetadataDto.setTisTriggerDetail(TIS_TRIGGER_DETAIL);

    DmsDto expectedDmsDto = new DmsDto(expectedPostDmsDto, expectedMetadataDto);

    assertEquals(expectedDmsDto, actualDmsDto);
  }

  @Test
  void shouldAssembleCurriculumMembership() {
    CurriculumMembershipDTO curriculumMembershipDto = new CurriculumMembershipDTO();
    curriculumMembershipDto.setCurriculumId(4L);
    curriculumMembershipDto.setId(1111L);
    curriculumMembershipDto.setCurriculumStartDate(LocalDate.of(2021, 2, 2));
    curriculumMembershipDto.setCurriculumEndDate(LocalDate.of(2022, 1, 1));
    curriculumMembershipDto.setCurriculumCompletionDate(LocalDate.of(2022, 1, 2));
    curriculumMembershipDto.setPeriodOfGrace(5);
    curriculumMembershipDto.setIntrepidId("12345");
    curriculumMembershipDto.setAmendedDate(LocalDateTime.of(2021, 1, 1, 1, 1, 1));

    CurriculumMembershipWrapperDto curriculumMembershipWrapperDto = new CurriculumMembershipWrapperDto(
        UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), curriculumMembershipDto);

    //when
    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(curriculumMembershipWrapperDto), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

    //then
    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);

    Object data = dmsDto.getData();
    assertThat("Unexpected data.", data, instanceOf(CurriculumMembershipDmsDto.class));

    CurriculumMembershipDmsDto curriculumMembershipDmsDto = (CurriculumMembershipDmsDto) data;
    assertThat("Unexpected id.",
        curriculumMembershipDmsDto.getId(),
        is("1111"));
    assertThat("Unexpected curriculum start date.",
        curriculumMembershipDmsDto.getCurriculumStartDate(),
        is("2021-02-02"));
    assertThat("Unexpected curriculum end date.",
        curriculumMembershipDmsDto.getCurriculumEndDate(),
        is("2022-01-01"));
    assertThat("Unexpected curriculum completion date.",
        curriculumMembershipDmsDto.getCurriculumCompletionDate(),
        is("2022-01-02"));
    assertThat("Unexpected period of grace.",
        curriculumMembershipDmsDto.getPeriodOfGrace(),
        is("5"));
    assertThat("Unexpected curriculum id.",
        curriculumMembershipDmsDto.getCurriculumId(),
        is("4"));
    assertThat("Unexpected intrepid id.",
        curriculumMembershipDmsDto.getIntrepidId(),
        is("12345"));
    assertThat("Unexpected amended date.",
        curriculumMembershipDmsDto.getAmendedDate(),
        is(LocalDateTime.of(2021, 1, 1, 1, 1, 1).toString()));
    assertThat("Unexpected programme membership UUID.",
        curriculumMembershipDmsDto.getProgrammeMembershipUuid(),
        is("123e4567-e89b-12d3-a456-426614174000"));

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key type.", metadata.getPartitionKeyType(),
        is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("CurriculumMembership"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());
  }

  @Test
  void shouldAssembleProgrammeMembership() {
    PersonDTO personDto = new PersonDTO();
    personDto.setId(1L);

    RotationDTO rotationDto = new RotationDTO();
    rotationDto.setId(2L);
    rotationDto.setName("a rotation");

    TrainingNumberDTO trainingNumberDto = new TrainingNumberDTO();
    trainingNumberDto.setId(3L);

    CurriculumMembershipDTO curriculumMembershipDto = new CurriculumMembershipDTO();
    curriculumMembershipDto.setCurriculumId(4L);
    curriculumMembershipDto.setId(1111L);
    curriculumMembershipDto.setCurriculumStartDate(LocalDate.of(2021, 2, 2));
    curriculumMembershipDto.setCurriculumEndDate(LocalDate.of(2022, 1, 1));
    curriculumMembershipDto.setCurriculumCompletionDate(LocalDate.of(2022, 1, 2));
    curriculumMembershipDto.setPeriodOfGrace(5);
    curriculumMembershipDto.setIntrepidId("12345");
    curriculumMembershipDto.setAmendedDate(LocalDateTime.of(3021, 1, 1, 1, 1, 1));

    ProgrammeMembershipDTO programmeMembershipDto = new ProgrammeMembershipDTO();
    programmeMembershipDto.setId(1111L);
    UUID programmeMembershipUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    programmeMembershipDto.setUuid(programmeMembershipUuid);
    programmeMembershipDto.setPerson(personDto);
    programmeMembershipDto.setProgrammeId(123L);
    programmeMembershipDto.setRotation(rotationDto);
    programmeMembershipDto.setTrainingNumber(trainingNumberDto);
    programmeMembershipDto.setTrainingPathway("a training pathway");
    programmeMembershipDto.setProgrammeMembershipType(ProgrammeMembershipType.SUBSTANTIVE);
    LocalDate startDate = LocalDate.of(2021, 1, 1);
    programmeMembershipDto.setProgrammeStartDate(startDate);
    LocalDate endDate = LocalDate.of(2022, 2, 2);
    programmeMembershipDto.setProgrammeEndDate(endDate);
    programmeMembershipDto.setLeavingReason("a leaving reason");
    programmeMembershipDto.setLeavingDestination("a leaving destination");
    programmeMembershipDto.setCurriculumMemberships(
        Collections.singletonList((curriculumMembershipDto)));
    LocalDateTime amendedDate = LocalDateTime.of(2021, 1, 1, 1, 1, 1);
    programmeMembershipDto.setAmendedDate(amendedDate);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(programmeMembershipDto), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);
    assertThat("Unexpected data type.", dmsDto.getData(),
        instanceOf(ProgrammeMembershipDmsDto.class));
    ProgrammeMembershipDmsDto programmeMembershipDmsDto = (ProgrammeMembershipDmsDto) dmsDto.getData();

    assertThat("Unexpected UUID.", programmeMembershipDmsDto.getUuid(),
        is(programmeMembershipUuid));
    assertThat("Unexpected person ID.", programmeMembershipDmsDto.getPersonId(), is("1"));
    assertThat("Unexpected programme ID.", programmeMembershipDmsDto.getProgrammeId(), is("123"));
    assertThat("Unexpected rotation ID.", programmeMembershipDmsDto.getRotationId(), is("2"));
    assertThat("Unexpected rotation.", programmeMembershipDmsDto.getRotation(), is("a rotation"));
    assertThat("Unexpected training number ID.", programmeMembershipDmsDto.getTrainingNumberId(),
        is("3"));
    assertThat("Unexpected programme membership type.",
        programmeMembershipDmsDto.getProgrammeMembershipType(),
        is(ProgrammeMembershipType.SUBSTANTIVE.toString()));
    assertThat("Unexpected programme start date.",
        programmeMembershipDmsDto.getProgrammeStartDate(), is(startDate));
    assertThat("Unexpected programme end date.", programmeMembershipDmsDto.getProgrammeEndDate(),
        is(endDate));
    assertThat("Unexpected leaving reason.", programmeMembershipDmsDto.getLeavingReason(),
        is("a leaving reason"));
    assertThat("Unexpected training pathway.", programmeMembershipDmsDto.getTrainingPathway(),
        is("a training pathway"));
    assertThat("Unexpected amended date.", programmeMembershipDmsDto.getAmendedDate(),
        is(amendedDate.toInstant(ZoneOffset.UTC)));
    assertThat("Unexpected leaving destination.", programmeMembershipDmsDto.getLeavingDestination(),
        is("a leaving destination"));

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key type.", metadata.getPartitionKeyType(),
        is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("ProgrammeMembership"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());
  }

  @Test
  @Disabled("Not yet implemented")
  void shouldAssembleQualification() {
    PersonDTO personDto = new PersonDTO();
    personDto.setId(100L);
    QualificationDTO qualificationDto = new QualificationDTO();
    qualificationDto.setId(1L);
    qualificationDto.setQualification("a qualification");
    qualificationDto.setIntrepidId("i2");
    qualificationDto.setQualificationType(QualificationType.HIGHER_QUALIFICATION);
    qualificationDto.setPerson(personDto);
    qualificationDto.setCountryOfQualification("a country");
    qualificationDto.setMedicalSchool("medical school");
    qualificationDto.setQualificationAttainedDate(LocalDate.MIN);
    qualificationDto.setAmendedDate(LocalDateTime.of(2021, 1, 1, 1, 1, 1));

    List<DmsDto> actualDmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(qualificationDto), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

    assertThat("Unexpected DTO count.", actualDmsDtos.size(), is(1));
    DmsDto actualDmsDto = actualDmsDtos.get(0);

    MetadataDto metadata = actualDmsDto.getMetadata();
    assertThat("Unexpected record type.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key.", metadata.getPartitionKeyType(), is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("Qualification"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());

    Object data = actualDmsDto.getData();
    assertThat("Unexpected data.", data, instanceOf(QualificationDmsDto.class));

    QualificationDmsDto qualificationDms = (QualificationDmsDto) data;
    assertThat("Unexpected record id.",
        qualificationDms.getId(), is("1"));
    assertThat("Unexpected record qualification.",
        qualificationDms.getQualification(), is("a qualification"));
    assertThat("Unexpected record intrepid id.",
        qualificationDms.getIntrepidId(), is("i2"));
    assertThat("Unexpected record qualification type.",
        qualificationDms.getQualificationType(),
        is(QualificationType.HIGHER_QUALIFICATION.toString()));
    assertThat("Unexpected record person id.",
        qualificationDms.getPersonId(), is("100"));
    assertThat("Unexpected record country of qualification.",
        qualificationDms.getCountryOfQualification(), is("a country"));
    assertThat("Unexpected record medical school.",
        qualificationDms.getMedicalSchool(), is("medical school"));
    assertThat("Unexpected record qualification attained date.",
        qualificationDms.getQualificationAttainedDate(), is(LocalDate.MIN.toString()));
    assertThat("Unexpected amended date.",
        qualificationDms.getAmendedDate(), is(LocalDateTime.of(2021, 1, 1, 1, 1, 1).toString()));
  }

  @Test
  void shouldAssembleADmsDtoWhenGivenATrustDto() {
    TrustDTO trustDto = new TrustDTO();
    trustDto.setCode("000");
    trustDto.setLocalOffice("someLocalOffice");
    trustDto.setStatus(CURRENT);
    trustDto.setTrustKnownAs("trustKnownAs");
    trustDto.setTrustName("trustName");
    trustDto.setTrustNumber("111");
    trustDto.setIntrepidId("222");
    trustDto.setId(333L);

    List<DmsDto> actualDmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(trustDto), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

    assertThat("Unexpected DTO count.", actualDmsDtos.size(), is(1));
    DmsDto actualDmsDto = actualDmsDtos.get(0);

    TrustDmsDto expectedTrustDmsDto = new TrustDmsDto();
    expectedTrustDmsDto.setCode("000");
    expectedTrustDmsDto.setLocalOffice("someLocalOffice");
    expectedTrustDmsDto.setStatus("CURRENT");
    expectedTrustDmsDto.setTrustKnownAs("trustKnownAs");
    expectedTrustDmsDto.setTrustName("trustName");
    expectedTrustDmsDto.setTrustNumber("111");
    expectedTrustDmsDto.setIntrepidId("222");
    expectedTrustDmsDto.setId("333");

    //inject the timestamp from the actualDmsDto into the expectedDmsDto
    String timestamp = actualDmsDto.getMetadata().getTimestamp();

    //inject the transaction-id from the actualDmsDto into the expectedDmsDto
    String transactionId = actualDmsDto.getMetadata().getTransactionId();

    MetadataDto expectedMetadataDto = new MetadataDto();
    expectedMetadataDto.setTimestamp(timestamp);
    expectedMetadataDto.setRecordType("data");
    expectedMetadataDto.setOperation("load");
    expectedMetadataDto.setPartitionKeyType("schema-table");
    expectedMetadataDto.setSchemaName("reference");
    expectedMetadataDto.setTableName("Trust");
    expectedMetadataDto.setTransactionId(transactionId);
    expectedMetadataDto.setTisTrigger(TIS_TRIGGER);
    expectedMetadataDto.setTisTriggerDetail(TIS_TRIGGER_DETAIL);

    DmsDto expectedDmsDto = new DmsDto(expectedTrustDmsDto, expectedMetadataDto);

    assertEquals(expectedDmsDto, actualDmsDto);
  }

  @Test
  void shouldHandleSite() {
    SiteDTO site = new SiteDTO();
    site.setId(40L);
    site.setIntrepidId("i40");
    site.setStartDate(LocalDate.MIN);
    site.setEndDate(LocalDate.MAX);
    site.setTrustId(140L);
    site.setTrustCode("TABC");
    site.setLocalOffice("some local office");
    site.setOrganisationalUnit("some org unit");
    site.setSiteCode("SABC");
    site.setSiteNumber("S123");
    site.setSiteName("Site Alpha Beta Charlie");
    site.setSiteKnownAs("Site ABC");
    site.setAddress("123 ABC Lane");
    site.setPostCode("AB12 3CD");
    site.setStatus(CURRENT);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(site), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected record type.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key.", metadata.getPartitionKeyType(), is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("reference"));
    assertThat("Unexpected table.", metadata.getTableName(), is("Site"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());

    Object data = dmsDto.getData();
    assertThat("Unexpected data.", data, instanceOf(SiteDmsDto.class));

    SiteDmsDto siteDms = (SiteDmsDto) data;
    assertThat("Unexpected record type.", siteDms.getId(), is("40"));
    assertThat("Unexpected record type.", siteDms.getIntrepidId(), is("i40"));
    assertThat("Unexpected record type.", siteDms.getStartDate(), is(LocalDate.MIN.toString()));
    assertThat("Unexpected record type.", siteDms.getEndDate(), is(LocalDate.MAX.toString()));
    assertThat("Unexpected record type.", siteDms.getLocalOffice(), is("some local office"));
    assertThat("Unexpected record type.", siteDms.getOrganisationalUnit(), is("some org unit"));
    assertThat("Unexpected record type.", siteDms.getTrustId(), is("140"));
    assertThat("Unexpected record type.", siteDms.getTrustCode(), is("TABC"));
    assertThat("Unexpected record type.", siteDms.getSiteCode(), is("SABC"));
    assertThat("Unexpected record type.", siteDms.getSiteNumber(), is("S123"));
    assertThat("Unexpected record type.", siteDms.getSiteName(), is("Site Alpha Beta Charlie"));
    assertThat("Unexpected record type.", siteDms.getSiteKnownAs(), is("Site ABC"));
    assertThat("Unexpected record type.", siteDms.getAddress(), is("123 ABC Lane"));
    assertThat("Unexpected record type.", siteDms.getPostCode(), is("AB12 3CD"));
    assertThat("Unexpected record type.", siteDms.getStatus(), is("CURRENT"));
  }

  @Test
  void shouldReturnEmptyWhenUnsupportedType() {
    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(new Object()), TIS_TRIGGER, TIS_TRIGGER_DETAIL);
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

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(programme), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

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

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(curriculum), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

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

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(specialty), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

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

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(placementSpecialty), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key.", metadata.getPartitionKeyType(), is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("tcs"));
    assertThat("Unexpected table.", metadata.getTableName(), is("PlacementSpecialty"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());

    Object data = dmsDto.getData();
    assertThat("Unexpected data.", data, instanceOf(PlacementSpecialtyDmsDto.class));

    PlacementSpecialtyDmsDto placementSpecialtyDmsDto = (PlacementSpecialtyDmsDto) data;
    assertThat("Unexpected placement id.", placementSpecialtyDmsDto.getPlacementId(), is("80"));
    assertThat("Unexpected specialty id.", placementSpecialtyDmsDto.getSpecialtyId(), is("90"));
    assertThat("Unexpected placement-specialty type.",
        placementSpecialtyDmsDto.getPlacementSpecialtyType(), is("PRIMARY"));
    assertThat("Unexpected specialty name.", placementSpecialtyDmsDto.getSpecialtyName(),
        is("specialtyName"));
  }

  @Test
  void shouldHandlePlacement() throws ParseException {
    PlacementSummaryDTO placementSummaryDto = new PlacementSummaryDTO();
    placementSummaryDto.setPlacementId(45L);
    placementSummaryDto.setDateFrom(DateUtils.parseDate("2020-01-01", "yyyy-MM-dd"));
    placementSummaryDto.setDateTo(DateUtils.parseDate("2022-02-02", "yyyy-MM-dd"));
    placementSummaryDto.setTraineeId(4500L);
    placementSummaryDto.setPlacementWholeTimeEquivalent(new BigDecimal(1));
    placementSummaryDto.setPostId(5L);
    placementSummaryDto.setGradeAbbreviation("gradeAbbreviation");
    placementSummaryDto.setPlacementType("placementType");
    placementSummaryDto.setStatus(PlacementStatus.CURRENT.toString());
    placementSummaryDto.setGradeId(20L);
    placementSummaryDto.setSiteId(30L);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(placementSummaryDto), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

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
    assertThat("Unexpected data.", data, instanceOf(PlacementSummaryDmsDto.class));

    PlacementSummaryDmsDto placementSummaryDmsDto = (PlacementSummaryDmsDto) data;
    assertThat("Unexpected id", "45", is(placementSummaryDmsDto.getId()));
    assertThat("Unexpected dateFrom", "2020-01-01",
        is(placementSummaryDmsDto.getDateFrom()));
    assertThat("Unexpected dateTo", "2022-02-02",
        is(placementSummaryDmsDto.getDateTo()));
    assertThat("Unexpected wholeTimeEquivalent", "1",
        is(placementSummaryDmsDto.getWholeTimeEquivalent()));
    assertThat("Unexpected traineeId", "4500", is(placementSummaryDmsDto.getTraineeId()));
    assertThat("Unexpected postId", "5", is(placementSummaryDmsDto.getPostId()));
    assertThat("Unexpected gradeAbbreviation", "gradeAbbreviation",
        is(placementSummaryDmsDto.getGradeAbbreviation()));
    assertThat("Unexpected placementType", "placementType",
        is(placementSummaryDmsDto.getPlacementType()));
    assertThat("Unexpected status", "CURRENT", is(placementSummaryDmsDto.getStatus()));
    assertThat("Unexpected gradeId", "20", is(placementSummaryDmsDto.getGradeId()));
    assertThat("Unexpected siteId", "30", is(placementSummaryDmsDto.getSiteId()));
  }

  @Test
  void shouldAssembleGrade() {
    GradeDTO grade = new GradeDTO();
    grade.setId(10L);
    grade.setIntrepidId("20");
    grade.setAbbreviation("abbreviation");
    grade.setName("name");
    grade.setLabel("label");
    grade.setStatus(com.transformuk.hee.tis.reference.api.enums.Status.CURRENT);
    grade.setPlacementGrade(true);
    grade.setPostGrade(false);
    grade.setTrainingGrade(true);

    List<DmsDto> dmsDtos = dmsRecordAssembler.assembleDmsDtos(
        singletonList(grade), TIS_TRIGGER, TIS_TRIGGER_DETAIL);

    assertThat("Unexpected DTO count.", dmsDtos.size(), is(1));
    DmsDto dmsDto = dmsDtos.get(0);
    assertThat("Unexpected data type.", dmsDto.getData(), instanceOf(GradeDmsDto.class));

    GradeDmsDto gradeDms = (GradeDmsDto) dmsDto.getData();
    assertThat("Unexpected id.", gradeDms.getId(), is("10"));
    assertThat("Unexpected intrepid id.", gradeDms.getIntrepidId(), is("20"));
    assertThat("Unexpected abbreviation.", gradeDms.getAbbreviation(), is("abbreviation"));
    assertThat("Unexpected name.", gradeDms.getName(), is("name"));
    assertThat("Unexpected label.", gradeDms.getLabel(), is("label"));
    assertThat("Unexpected status.", gradeDms.getStatus(),
        is(com.transformuk.hee.tis.reference.api.enums.Status.CURRENT.toString()));
    assertThat("Unexpected placement grade.", gradeDms.getPlacementGrade(), is("1"));
    assertThat("Unexpected post grade.", gradeDms.getPostGrade(), is("0"));
    assertThat("Unexpected post grade.", gradeDms.getTrainingGrade(), is("1"));

    MetadataDto metadata = dmsDto.getMetadata();
    assertThat("Unexpected timestamp.", metadata.getTimestamp(), notNullValue());
    assertThat("Unexpected record type.", metadata.getRecordType(), is("data"));
    assertThat("Unexpected operation.", metadata.getOperation(), is("load"));
    assertThat("Unexpected partition key type.", metadata.getPartitionKeyType(),
        is("schema-table"));
    assertThat("Unexpected schema.", metadata.getSchemaName(), is("reference"));
    assertThat("Unexpected table.", metadata.getTableName(), is("Grade"));
    assertThat("Unexpected transaction id.", metadata.getTransactionId(), notNullValue());
  }
}
