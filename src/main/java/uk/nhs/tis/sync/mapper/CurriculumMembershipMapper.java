package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.tis.sync.dto.CurriculumMembershipDmsDto;

@Mapper(componentModel = "spring")
public interface CurriculumMembershipMapper extends
    DmsMapper<CurriculumMembershipDTO, CurriculumMembershipDmsDto> {

  /**
   * Converts a CurriculumMembershipDTO to a CurriculumMembershipDmsDto.
   *
   * @param curriculumMembershipDto the CurriculumMembershipDTO to convert
   * @return the CurriculumMembershipDmsDto
   */
  @Mapping(target = "rotation", ignore = true)
  @Mapping(target = "rotationId", ignore = true)
  @Mapping(target = "programmeMembershipUuid", ignore = true)
  @Mapping(target = "personId", ignore = true)
  @Mapping(target = "programmeId", ignore = true)
  @Mapping(target = "trainingNumberId", ignore = true)
  @Mapping(target = "trainingPathway", ignore = true)
  @Mapping(target = "programmeMembershipType", ignore = true)
  @Mapping(target = "programmeStartDate", ignore = true)
  @Mapping(target = "programmeEndDate", ignore = true)
  @Mapping(target = "leavingReason", ignore = true)
  @Mapping(target = "leavingDestination", ignore = true)
  CurriculumMembershipDmsDto toDmsDto(CurriculumMembershipDTO curriculumMembershipDto);
}
