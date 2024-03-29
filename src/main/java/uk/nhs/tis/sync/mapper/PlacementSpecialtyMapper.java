package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import org.mapstruct.Mapper;
import uk.nhs.tis.sync.dto.PlacementSpecialtyDmsDto;

@Mapper(componentModel = "spring")
public interface PlacementSpecialtyMapper extends
    DmsMapper<PlacementSpecialtyDTO, PlacementSpecialtyDmsDto> {

  PlacementSpecialtyDmsDto toDmsDto(PlacementSpecialtyDTO placementSpecialtyDto);
}
