package uk.nhs.tis.sync.service;

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.dto.PostDataDmsDto;
import uk.nhs.tis.sync.dto.TrustDataDmsDto;
import uk.nhs.tis.sync.mapper.PostDtoToDataDmsDtoMapperImpl;
import uk.nhs.tis.sync.mapper.TrustDtoToDataDmsDtoMapperImpl;
import uk.nhs.tis.sync.mapper.util.PostDataDmsDtoUtil;
import uk.nhs.tis.sync.mapper.util.TrustDataDmsDtoUtil;

import java.lang.reflect.Field;

import static com.transformuk.hee.tis.reference.api.enums.Status.CURRENT;
import static org.junit.Assert.assertEquals;

public class DmsRecordAssemblerTest {

  private PostDtoToDataDmsDtoMapperImpl postDtoToDataDmsDtoMapperImpl;

  private TrustDtoToDataDmsDtoMapperImpl trustDtoToDataDmsDtoMapperImpl;

  private DmsRecordAssembler dmsRecordAssembler;

  private PostDTO postDto;

  private TrustDTO trustDto;

  @Before
  public void setUp() {
    postDtoToDataDmsDtoMapperImpl = new PostDtoToDataDmsDtoMapperImpl();
    Field fieldPost = ReflectionUtils.findField(PostDtoToDataDmsDtoMapperImpl.class,
        "postDataDmsDtoUtil");
    fieldPost.setAccessible(true);
    ReflectionUtils.setField(fieldPost, postDtoToDataDmsDtoMapperImpl, new PostDataDmsDtoUtil());

    trustDtoToDataDmsDtoMapperImpl = new TrustDtoToDataDmsDtoMapperImpl();
    Field fieldTrust = ReflectionUtils.findField(TrustDtoToDataDmsDtoMapperImpl.class,
        "trustDataDmsDtoUtil");
    fieldTrust.setAccessible(true);
    ReflectionUtils.setField(fieldTrust, trustDtoToDataDmsDtoMapperImpl, new TrustDataDmsDtoUtil());

    dmsRecordAssembler = new DmsRecordAssembler(postDtoToDataDmsDtoMapperImpl,
        trustDtoToDataDmsDtoMapperImpl);

    PostDTO newPost = new PostDTO();
    newPost.setId(184668L);

    postDto = new PostDTO();
    postDto.setId(44381L);
    postDto.setNationalPostNumber("EAN/8EJ83/094/SPR/001");
    postDto.status(Status.CURRENT);
    postDto.employingBodyId(287L);
    postDto.trainingBodyId(1464L);
    postDto.newPost(newPost);
    postDto.oldPost(null);
    postDto.owner("Health Education England North West London");
    postDto.intrepidId("128374444");

    trustDto = new TrustDTO();
    trustDto.setCode("000");
    trustDto.setLocalOffice("someLocalOffice");
    trustDto.setStatus(CURRENT);
    trustDto.setTrustKnownAs("trustKnownAs");
    trustDto.setTrustName("trustName");
    trustDto.setTrustNumber("111");
    trustDto.setIntrepidId("222");
    trustDto.setId(333L);
  }

  @Test
  public void shouldAssembleADmsDtoWhenGivenAPostDto() {
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

    //ingect the transaction-id from the actualDmsDto into the expectedDmsDto
    String transactionId = actualDmsDto.getMetadata().getTransactionId();

    MetadataDto expectedMetadataDto = new MetadataDto(timestamp,
        "data",
        "load",
        "schema-table",
        "tcs",
        "Post",
        transactionId);

    DmsDto expectedDmsDto = new DmsDto(expectedPostDataDmsDto, expectedMetadataDto);

    assertEquals(expectedDmsDto, actualDmsDto);
  }

  @Test
  public void shouldAssembleADmsDtoWhenGivenATrustDto() {
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

    //ingect the transaction-id from the actualDmsDto into the expectedDmsDto
    String transactionId = actualDmsDto.getMetadata().getTransactionId();

    MetadataDto expectedMetadataDto = new MetadataDto(timestamp,
        "data",
        "load",
        "schema-table",
        "reference",
        "Trust",
        transactionId);

    DmsDto expectedDmsDto = new DmsDto(expectedTrustDataDmsDto, expectedMetadataDto);

    assertEquals(expectedDmsDto, actualDmsDto);
  }
}
