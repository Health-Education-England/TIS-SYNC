package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.PlacementSummaryDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.tis.sync.dto.PlacementSummaryDmsDto;

/**
 * A DMS mapper for converting PlacementSummaryDTOs into PlacementSummaryDmsDtos.
 */
@Mapper(componentModel = "spring")
public interface PlacementSummaryMapper extends
    DmsMapper<PlacementSummaryDTO, PlacementSummaryDmsDto> {

  @Mapping(target = "id", source = "placementId")
  @Mapping(target = "wholeTimeEquivalent", source = "placementWholeTimeEquivalent")
  @Mapping(target = "dateFrom", dateFormat = "yyyy-MM-dd")
  @Mapping(target = "dateTo", dateFormat = "yyyy-MM-dd")
  PlacementSummaryDmsDto toDmsDto(PlacementSummaryDTO placementSummaryDto);
}
