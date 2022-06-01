package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import org.mapstruct.Mapper;
import uk.nhs.tis.sync.dto.ProgrammeMembershipDmsDto;

@Mapper(componentModel = "spring")
public interface CurriculumMembershipMapper {

  /**
   * Converts a CurriculumMembershipDTO to a ProgrammeMembershipDmsDto.
   *
   * @param curriculumMembershipDto   the CurriculumMembershipDTO to convert
   * @return                          the ProgrammeMembershipDmsDto
   */
  ProgrammeMembershipDmsDto toDmsDto(CurriculumMembershipDTO curriculumMembershipDto);
}
