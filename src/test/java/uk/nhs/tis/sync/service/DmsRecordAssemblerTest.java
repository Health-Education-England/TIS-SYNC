package uk.nhs.tis.sync.service;

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.Before;
import org.junit.Test;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.dto.PostDataDmsDto;
import uk.nhs.tis.sync.dto.TrustDataDmsDto;

import static com.transformuk.hee.tis.reference.api.enums.Status.CURRENT;
import static org.junit.Assert.assertEquals;

public class DmsRecordAssemblerTest {

  private DmsRecordAssembler dmsRecordAssembler;

  private PostDTO postDto;

  private TrustDTO trustDto;

  @Before
  public void setUp() {
    dmsRecordAssembler = new DmsRecordAssembler();

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

    PostDataDmsDto expectedPostDataDmsDto = new PostDataDmsDto("44381",
        "EAN/8EJ83/094/SPR/001",
        "CURRENT",
        "287",
        "1464",
        null,
        "184668",
        "Health Education England North West London",
        "128374444"
    );

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

    TrustDataDmsDto expectedTrustDataDmsDto = new TrustDataDmsDto("000",
        "someLocalOffice",
        "CURRENT",
        "trustKnownAs",
        "trustName",
        "111",
        "222",
        "333");

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
