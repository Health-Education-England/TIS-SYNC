package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.dto.TrustDataDmsDto;

@Component
public class TrustDtoToDataDmsDtoMapper {

  /**
   * Method to map a TrustDto to a TrustDataDmsDto.
   * @param trustDto The original TrustDto
   * @return        The TrustDataDmsDto mapped from the TrustDto
   */
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
