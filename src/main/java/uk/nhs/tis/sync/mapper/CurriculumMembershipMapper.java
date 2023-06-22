package uk.nhs.tis.sync.mapper;

import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.tis.sync.dto.CurriculumMembershipDmsDto;
import uk.nhs.tis.sync.dto.CurriculumMembershipWrapperDto;

@Mapper(componentModel = "spring")
public interface CurriculumMembershipMapper extends
    DmsMapper<CurriculumMembershipWrapperDto, CurriculumMembershipDmsDto> {

  /**
   * Converts a CurriculumMembershipDTO to a CurriculumMembershipDmsDto.
   *
   * @param curriculumMembershipWrapperDto the FindBetterNameDto to convert
   * @return the CurriculumMembershipDmsDto
   */
  @Mapping(target = "id", source = "curriculumMembership.id")
  @Mapping(target = "curriculumStartDate", source = "curriculumMembership.curriculumStartDate")
  @Mapping(target = "curriculumEndDate", source = "curriculumMembership.curriculumEndDate")
  @Mapping(target = "curriculumCompletionDate",
      source = "curriculumMembership.curriculumCompletionDate")
  @Mapping(target = "periodOfGrace", source = "curriculumMembership.periodOfGrace")
  @Mapping(target = "curriculumId", source = "curriculumMembership.curriculumId")
  @Mapping(target = "intrepidId", source = "curriculumMembership.intrepidId")
  @Mapping(target = "programmeMembershipUuid")
  @Mapping(target = "amendedDate", source = "curriculumMembership.amendedDate")
  CurriculumMembershipDmsDto toDmsDto(
      CurriculumMembershipWrapperDto curriculumMembershipWrapperDto);

  /**
   * Maps a UUID to string.
   *
   * @param source The UUID to map.
   * @return The string value of the UUID.
   */
  default String map(UUID source) {
    return source == null ? null : source.toString();
  }
}
