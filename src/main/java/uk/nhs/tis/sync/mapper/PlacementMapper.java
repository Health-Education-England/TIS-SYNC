package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.PlacementDTO;
import org.mapstruct.Mapper;
import uk.nhs.tis.sync.dto.PlacementDmsDto;

@Mapper(componentModel = "spring")
public interface PlacementMapper {

  PlacementDmsDto toDmsDto(PlacementDTO placementDto);
}
