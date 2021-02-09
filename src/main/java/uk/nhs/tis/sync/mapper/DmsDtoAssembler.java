package uk.nhs.tis.sync.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import uk.nhs.tis.sync.dto.DmsDto;
import uk.nhs.tis.sync.dto.MetadataDto;
import uk.nhs.tis.sync.dto.PostDataDmsDto;
import uk.nhs.tis.sync.dto.TrustDataDmsDto;

import java.time.LocalDateTime;

public class DmsDtoAssembler {

  private PostDtoToDataDmsDtoMapper postDtoToDataDmsDtoMapper;

  private TrustDtoToDataDmsDtoMapper trustDtoToDataDmsDtoMapper;

  private ObjectMapper objectMapper;

  public DmsDtoAssembler(PostDtoToDataDmsDtoMapper postDtoToDataDmsDtoMapper,
                         TrustDtoToDataDmsDtoMapper trustDtoToDataDmsDtoMapper,
                         ObjectMapper objectMapper) {
    this.postDtoToDataDmsDtoMapper = postDtoToDataDmsDtoMapper;
    this.trustDtoToDataDmsDtoMapper = trustDtoToDataDmsDtoMapper;
    this.objectMapper = new ObjectMapper();
  }

  public String buildRecord(Object dto) throws JsonProcessingException {
    String stringifiedDmsDto = null;

    if (dto instanceof PostDTO) {
      PostDataDmsDto postDataDmsDto = postDtoToDataDmsDtoMapper.postDtoToDataDmsDto((PostDTO) dto);
      MetadataDto metadataDto = new MetadataDto(
          LocalDateTime.now().toString(),
          "data",
          "load",
          "schema-table",
          "tcs",
          "Post",
          "transactionId"
      );

      DmsDto dmsDto = new DmsDto(postDataDmsDto, metadataDto);
      stringifiedDmsDto = objectMapper.writeValueAsString(dmsDto);
    }

    if (dto instanceof TrustDTO) {
      TrustDataDmsDto trustDataDmsDto = trustDtoToDataDmsDtoMapper.trustDtoToDataDmsDto((TrustDTO) dto);
      MetadataDto metadataDto = new MetadataDto(
          LocalDateTime.now().toString(),
          "data",
          "load",
          "schema-table",
          "reference",
          "Trust",
          "transactionId"
      );

      DmsDto dmsDto = new DmsDto(trustDataDmsDto, metadataDto);
      stringifiedDmsDto = objectMapper.writeValueAsString(dmsDto);
    }

    return stringifiedDmsDto;
  }
}
