package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.QualificationDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.QualificationType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.nhs.tis.sync.dto.QualificationDmsDto;

/**
 * A DMS mapper for converting QualificationDTOs into QualificationDmsDtos.
 */
@Mapper(componentModel = "spring")
public interface QualificationMapper extends
    DmsMapper<QualificationDTO, QualificationDmsDto> {
  @Mapping(target = "personId", source = "person.id")
  @Mapping(target = "qualificationType", source = "qualificationType",
      qualifiedByName = "getQualificationType")
  QualificationDmsDto toDmsDto(QualificationDTO qualificationDto);

  /**
   * A helper function for converting qualificationType into a String.
   *
   * @param qualificationType the QualificationType to convert
   * @return the QualificationType as a String
   */
  @Named("getQualificationType")
  default String getQualificationType(QualificationType qualificationType) {
    if (qualificationType != null) {
      return qualificationType.toString();
    }
    return null;
  }
}
