package uk.nhs.tis.sync.service;

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.dto.PostDataDmsDto;
import uk.nhs.tis.sync.dto.TrustDataDmsDto;
import uk.nhs.tis.sync.mapper.PostDtoToPostDataDmsDtoMapper;
import uk.nhs.tis.sync.mapper.TrustDtoToTrustDataDmsDtoMapper;

@Component
public class DmsRecordAssembler {

  private static final String LOAD = "load";

  public static final String DATA = "data";

  public static final String PARTITION_KEY_TYPE = "schema-table";

  private PostDtoToPostDataDmsDtoMapper postDtoToPostDataDmsDtoMapper;

  private TrustDtoToTrustDataDmsDtoMapper trustDtoToTrustDataDmsDtoMapper;

  /**
   * Constructor for a DmsRecordAssembler, which instantiates the relevant mappers.
   */
  public DmsRecordAssembler(PostDtoToPostDataDmsDtoMapper postDtoToPostDataDmsDtoMapper,
                            TrustDtoToTrustDataDmsDtoMapper trustDtoToTrustDataDmsDtoMapper) {
    this.postDtoToPostDataDmsDtoMapper = postDtoToPostDataDmsDtoMapper;
    this.trustDtoToTrustDataDmsDtoMapper = trustDtoToTrustDataDmsDtoMapper;
  }

  /**
   * The method that assembles a complete DmsDto starting from a dto (e.g. a PostDto or a TrustDto)
   * @param dto The dto which will be mapped to a -DataDmsDto (e.g. a PostDataDmsDto, or a
   *            TrustDataDmsDto)
   * @return    The DmsDto, complete with data and metadata.
   */
  public DmsDto assembleDmsDto(Object dto) {
    DmsDto dmsDto = null;

    if (dto instanceof PostDTO) {
      PostDataDmsDto postDataDmsDto = postDtoToPostDataDmsDtoMapper
          .postDtoToPostDataDmsDto((PostDTO) dto);
      MetadataDto metadataDto = new MetadataDto(
          Instant.now().toString(),
          DATA,
          LOAD,
          PARTITION_KEY_TYPE,
          "tcs",
          "Post",
          UUID.randomUUID().toString()
      );

      dmsDto = new DmsDto(postDataDmsDto, metadataDto);
    }

    if (dto instanceof TrustDTO) {
      TrustDataDmsDto trustDataDmsDto = trustDtoToTrustDataDmsDtoMapper
          .trustDtoToTrustDataDmsDto((TrustDTO) dto);
      MetadataDto metadataDto = new MetadataDto(
          Instant.now().toString(),
          DATA,
          LOAD,
          PARTITION_KEY_TYPE,
          "reference",
          "Trust",
          UUID.randomUUID().toString()
      );

      dmsDto = new DmsDto(trustDataDmsDto, metadataDto);
    }

    return dmsDto;
  }
}
