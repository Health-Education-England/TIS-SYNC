package uk.nhs.tis.sync.service;

import static com.transformuk.hee.tis.reference.api.enums.Status.CURRENT;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.AssessmentType;
import com.transformuk.hee.tis.tcs.api.enumeration.CurriculumSubType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.nhs.tis.sync.dto.CurriculumDmsDto;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.dto.PostDmsDto;
import uk.nhs.tis.sync.dto.ProgrammeDmsDto;
import uk.nhs.tis.sync.dto.SiteDmsDto;
import uk.nhs.tis.sync.dto.TrustDmsDto;
import uk.nhs.tis.sync.mapper.CurriculumMapper;
import uk.nhs.tis.sync.mapper.PostMapperImpl;
import uk.nhs.tis.sync.mapper.ProgrammeMapper;
import uk.nhs.tis.sync.mapper.SiteMapper;
import uk.nhs.tis.sync.mapper.TrustMapperImpl;

class DmsRecordAssemblerTest {

  private DmsRecordAssembler dmsRecordAssembler;

  @BeforeEach
  void setUp() {
    PostMapperImpl postMapper = new PostMapperImpl();
    TrustMapperImpl trustMapper = new TrustMapperImpl();
    SiteMapper siteMapper = Mappers.getMapper(SiteMapper.class);
    ProgrammeMapper programmeMapper = Mappers.getMapper(ProgrammeMapper.class);

    CurriculumMapper curriculumMapper = Mappers.getMapper(CurriculumMapper.class);

    dmsRecordAssembler = new DmsRecordAssembler(postMapper, trustMapper, siteMapper,
        programmeMapper, curriculumMapper);
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

    DmsDto actualDmsDto = dmsRecordAssembler.assembleDmsDto(postDto);

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

    DmsDto actualDmsDto = dmsRecordAssembler.assembleDmsDto(trustDto);

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

    DmsDto dmsDto = dmsRecordAssembler.assembleDmsDto(site);

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
  void shouldReturnNullWhenUnsupportedType() {
    DmsDto dmsDto = dmsRecordAssembler.assembleDmsDto(new Object());
    assertThat("Unexpected dms dto.", dmsDto, nullValue());

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

    DmsDto dmsDto = dmsRecordAssembler.assembleDmsDto(programme);

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

    DmsDto dmsDto = dmsRecordAssembler.assembleDmsDto(curriculum);

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
}
