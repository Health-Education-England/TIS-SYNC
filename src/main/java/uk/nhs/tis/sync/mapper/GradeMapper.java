package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.reference.api.dto.GradeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.nhs.tis.sync.dto.GradeDmsDto;

/**
 * A mapper to map between TCS and DMS DTOs for the Grade data type.
 */
@Mapper(componentModel = "spring")
public interface GradeMapper extends DmsMapper<GradeDTO, GradeDmsDto> {

  @Mapping(target = "placementGrade", source = "placementGrade",
      qualifiedByName = "getBooleanAsZeroOrOne")
  @Mapping(target = "trainingGrade", source = "trainingGrade",
      qualifiedByName = "getBooleanAsZeroOrOne")
  @Mapping(target = "postGrade", source = "postGrade",
      qualifiedByName = "getBooleanAsZeroOrOne")
  GradeDmsDto toDmsDto(GradeDTO gradeDto);

  @Named("getBooleanAsZeroOrOne")
  default String getBooleanAsZeroOrOne(Boolean bool) {
    if (bool != null) {
      return bool ? "1" : "0";
    }
    return null;
  }
}
