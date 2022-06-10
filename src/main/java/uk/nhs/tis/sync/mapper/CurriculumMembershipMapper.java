package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
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

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "curriculumStartDate", ignore = true)
  @Mapping(target = "curriculumEndDate", ignore = true)
  @Mapping(target = "curriculumCompletionDate", ignore = true)
  @Mapping(target = "periodOfGrace", ignore = true)
  @Mapping(target = "curriculumId", ignore = true)
  @Mapping(target = "intrepidId", ignore = true)
  @Mapping(target = "amendedDate", ignore = true)
  CurriculumMembershipDmsDto update(@MappingTarget CurriculumMembershipDmsDto target,
                                    CurriculumMembershipDmsDto source);
}
