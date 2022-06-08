package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;
import uk.nhs.tis.sync.dto.CurriculumMembershipDmsDto;

@Mapper(componentModel = "spring")
public interface CurriculumMembershipMapper {

  /**
   * Converts a CurriculumMembershipDTO to a CurriculumMembershipDmsDto.
   *
   * @param curriculumMembershipDto the CurriculumMembershipDTO to convert
   * @return the CurriculumMembershipDmsDto
   */
  CurriculumMembershipDmsDto toDmsDto(CurriculumMembershipDTO curriculumMembershipDto);

  /**
   * Adds ProgrammeMembership details to an existing CurriculumMembershipDmsDto.
   *
   * @param programmeMembershipDto the ProgrammeMembershipDTO to incorporate
   * @param dmsDto                 the CurriculumMembershipDmsDto to augment
   */
  default void setProgrammeMembershipDetails(ProgrammeMembershipDTO programmeMembershipDto,
                                             @MappingTarget CurriculumMembershipDmsDto dmsDto) {

    ProgrammeMembershipMapper programmeMembershipMapper
        = Mappers.getMapper(ProgrammeMembershipMapper.class);
    CurriculumMembershipDmsDto dmsDtoPmDetails
        = programmeMembershipMapper.toDmsDto(programmeMembershipDto);
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
