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

  /**
   * Adds ProgrammeMembership details to an existing CurriculumMembershipDmsDto.
   *
   * @param dmsDtoPmDetails the CurriculumMembershipDmsDto with programme membership to incorporate
   * @param dmsDto          the CurriculumMembershipDmsDto to augment
   */
  default void addProgrammeMembershipDetails(CurriculumMembershipDmsDto dmsDtoPmDetails,
                                             @MappingTarget CurriculumMembershipDmsDto dmsDto) {
    if (dmsDtoPmDetails != null) {
      dmsDto.setProgrammeMembershipUuid(dmsDtoPmDetails.getProgrammeMembershipUuid());
      dmsDto.setPersonId(dmsDtoPmDetails.getPersonId());
      dmsDto.setProgrammeId(dmsDtoPmDetails.getProgrammeId());
      dmsDto.setRotationId(dmsDtoPmDetails.getRotationId());
      dmsDto.setRotation(dmsDtoPmDetails.getRotation());
      dmsDto.setTrainingNumberId(dmsDtoPmDetails.getTrainingNumberId());
      dmsDto.setTrainingPathway(dmsDtoPmDetails.getTrainingPathway());
      dmsDto.setProgrammeMembershipType(dmsDtoPmDetails.getProgrammeMembershipType());
      dmsDto.setProgrammeStartDate(dmsDtoPmDetails.getProgrammeStartDate());
      dmsDto.setProgrammeEndDate(dmsDtoPmDetails.getProgrammeEndDate());
      dmsDto.setLeavingReason(dmsDtoPmDetails.getLeavingReason());
      dmsDto.setLeavingDestination(dmsDtoPmDetails.getLeavingDestination());
    }
  }
}
