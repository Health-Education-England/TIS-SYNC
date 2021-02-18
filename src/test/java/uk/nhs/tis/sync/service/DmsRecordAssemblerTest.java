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
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import java.lang.reflect.Field;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.util.ReflectionUtils;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.dto.PostDataDmsDto;
import uk.nhs.tis.sync.dto.SiteDmsDto;
import uk.nhs.tis.sync.dto.TrustDataDmsDto;
import uk.nhs.tis.sync.mapper.PostDtoToPostDataDmsDtoMapperImpl;
import uk.nhs.tis.sync.mapper.SiteMapper;
import uk.nhs.tis.sync.mapper.TrustDtoToTrustDataDmsDtoMapperImpl;
import uk.nhs.tis.sync.mapper.util.PostDataDmsDtoUtil;
import uk.nhs.tis.sync.mapper.util.TrustDataDmsDtoUtil;

class DmsRecordAssemblerTest {

  private DmsRecordAssembler dmsRecordAssembler;

  @BeforeEach
  void setUp() {
    PostDtoToPostDataDmsDtoMapperImpl postMapper = new PostDtoToPostDataDmsDtoMapperImpl();
    Field fieldPost = ReflectionUtils
        .findField(PostDtoToPostDataDmsDtoMapperImpl.class, "postDataDmsDtoUtil");
    fieldPost.setAccessible(true);
    ReflectionUtils.setField(fieldPost, postMapper, new PostDataDmsDtoUtil());

    TrustDtoToTrustDataDmsDtoMapperImpl trustMapper = new TrustDtoToTrustDataDmsDtoMapperImpl();
    Field fieldTrust = ReflectionUtils
        .findField(TrustDtoToTrustDataDmsDtoMapperImpl.class, "trustDataDmsDtoUtil");
    fieldTrust.setAccessible(true);
    ReflectionUtils.setField(fieldTrust, trustMapper, new TrustDataDmsDtoUtil());

    SiteMapper siteMapper = Mappers.getMapper(SiteMapper.class);

    dmsRecordAssembler = new DmsRecordAssembler(postMapper, trustMapper, siteMapper);
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

    PostDataDmsDto expectedPostDataDmsDto = new PostDataDmsDto();
    expectedPostDataDmsDto.setId("44381");
    expectedPostDataDmsDto.setNationalPostNumber("EAN/8EJ83/094/SPR/001");
    expectedPostDataDmsDto.setStatus("CURRENT");
    expectedPostDataDmsDto.setEmployingBodyId("287");
    expectedPostDataDmsDto.setTrainingBodyId("1464");
    expectedPostDataDmsDto.setOldPostId(null);
    expectedPostDataDmsDto.setNewPostId("184668");
    expectedPostDataDmsDto.setOwner("Health Education England North West London");
    expectedPostDataDmsDto.setIntrepidId("128374444");

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

    DmsDto expectedDmsDto = new DmsDto(expectedPostDataDmsDto, expectedMetadataDto);

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

    TrustDataDmsDto expectedTrustDataDmsDto = new TrustDataDmsDto();
    expectedTrustDataDmsDto.setCode("000");
    expectedTrustDataDmsDto.setLocalOffice("someLocalOffice");
    expectedTrustDataDmsDto.setStatus("CURRENT");
    expectedTrustDataDmsDto.setTrustKnownAs("trustKnownAs");
    expectedTrustDataDmsDto.setTrustName("trustName");
    expectedTrustDataDmsDto.setTrustNumber("111");
    expectedTrustDataDmsDto.setIntrepidId("222");
    expectedTrustDataDmsDto.setId("333");

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

    DmsDto expectedDmsDto = new DmsDto(expectedTrustDataDmsDto, expectedMetadataDto);

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
}
