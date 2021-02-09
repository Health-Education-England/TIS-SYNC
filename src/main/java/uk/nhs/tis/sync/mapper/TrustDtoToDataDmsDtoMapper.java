package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import uk.nhs.tis.sync.dto.TrustDataDmsDto;

public class TrustDtoToDataDmsDtoMapper {

  public TrustDataDmsDto trustDtoToDataDmsDto(TrustDTO trustDto) {
    return new TrustDataDmsDto(
        trustDto.getCode(),
        trustDto.getLocalOffice(),
        trustDto.getStatus().toString(),
        trustDto.getTrustKnownAs(),
        trustDto.getTrustName(),
        trustDto.getTrustNumber(),
        trustDto.getIntrepidId(),
        String.valueOf(trustDto.getId())
    );
  }
}
