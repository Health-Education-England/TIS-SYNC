package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import org.mapstruct.Mapper;
import uk.nhs.tis.sync.dto.PlacementDetailsDmsDto;

@Mapper(componentModel = "spring")
public interface PlacementDetailsMapper {

  PlacementDetailsDmsDto toDmsDto(PlacementDetailsDTO placementDetailsDto);
}
