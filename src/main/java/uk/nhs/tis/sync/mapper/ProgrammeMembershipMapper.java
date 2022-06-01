package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import java.util.UUID;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import uk.nhs.tis.sync.dto.ProgrammeMembershipDmsDto;

@Mapper(componentModel = "spring",
        uses = {CurriculumMembershipMapper.class})
public interface ProgrammeMembershipMapper {

  /**
   * Converts a ProgrammeMembershipDTO to a ProgrammeMembershipDmsDto.
   *
   * <p>Note that the ProgrammeMembershipDTO should have exactly one child CurriculumMembershipDTO,
   * i.e. it should be a non-normalised ProgrammeMembershipDTO, not one which has been normalised by
   * ProgrammeMembershipServiceImpl.findProgrammeMembershipsForTraineeRolledUp()</p>
   *
   * @param programmeMembershipDto  the ProgrammeMembershipDTO to convert
   * @return                        the ProgrammeMembershipDmsDto
   */

  @Mapping(target = "rotation", source = "rotation.name")
  @Mapping(target = "rotationId", source = "rotation.id")
  @Mapping(target = "programmeMembershipUuid", source = "uuid",
      qualifiedByName = "getProgrammeMembershipUuid")
  @Mapping(target = "personId", source = "person.id")
  @Mapping(target = "trainingNumberId", source = "trainingNumber.id")
  @Mapping(target = "programmeMembershipType", source = "programmeMembershipType",
      qualifiedByName = "getProgrammeMembershipType")
  @Mapping(target = "curriculumStartDate", ignore = true)
  @Mapping(target = "curriculumEndDate", ignore = true)
  @Mapping(target = "curriculumCompletionDate", ignore = true)
  @Mapping(target = "periodOfGrace", ignore = true)
  @Mapping(target = "curriculumId", ignore = true)
  @Mapping(target = "intrepidId", ignore = true)
  ProgrammeMembershipDmsDto toDmsDto(ProgrammeMembershipDTO programmeMembershipDto);

  @Named("getProgrammeMembershipUuid")
  default String getProgrammeMembershipUuid(UUID uuid) {
    if (uuid != null) {
      return uuid.toString();
    }
    return null;
  }

  @Named("getProgrammeMembershipType")
  default String getProgrammeMembershipType(ProgrammeMembershipType programmeMembershipType) {
    if (programmeMembershipType != null) {
      return programmeMembershipType.toString();
    }
    return null;
  }

  @AfterMapping
  default void setCurriculumDetails(ProgrammeMembershipDTO programmeMembershipDto,
                                    @MappingTarget ProgrammeMembershipDmsDto dmsDto) {
    if (programmeMembershipDto.getCurriculumMemberships() != null) {
      CurriculumMembershipDTO curriculumMembershipDto
          = programmeMembershipDto.getCurriculumMemberships().get(0);

      CurriculumMembershipMapper curriculumMembershipMapper
          = Mappers.getMapper(CurriculumMembershipMapper.class);
      ProgrammeMembershipDmsDto dmsDtoCurriculumDetails
          = curriculumMembershipMapper.toDmsDto(curriculumMembershipDto);
      dmsDto.setCurriculumStartDate(dmsDtoCurriculumDetails.getCurriculumStartDate());
      dmsDto.setCurriculumEndDate(dmsDtoCurriculumDetails.getCurriculumEndDate());
      dmsDto.setCurriculumCompletionDate(dmsDtoCurriculumDetails.getCurriculumCompletionDate());
      dmsDto.setPeriodOfGrace(dmsDtoCurriculumDetails.getPeriodOfGrace());
      dmsDto.setCurriculumId(dmsDtoCurriculumDetails.getCurriculumId());
      dmsDto.setIntrepidId(dmsDtoCurriculumDetails.getIntrepidId());
    }
  }
}
