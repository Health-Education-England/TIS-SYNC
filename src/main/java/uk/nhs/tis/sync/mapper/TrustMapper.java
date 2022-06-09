package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import org.mapstruct.Mapper;
import uk.nhs.tis.sync.dto.TrustDmsDto;

@Mapper(componentModel = "spring")
public interface TrustMapper extends DmsMapper<TrustDTO, TrustDmsDto> {

  TrustDmsDto toDmsDto(TrustDTO trustDto);
}
