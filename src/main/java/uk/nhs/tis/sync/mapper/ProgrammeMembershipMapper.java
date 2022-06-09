package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumMembershipDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.nhs.tis.sync.dto.CurriculumMembershipDmsDto;

@Mapper(componentModel = "spring")
public interface ProgrammeMembershipMapper extends
    DmsMapper<ProgrammeMembershipDTO, CurriculumMembershipDmsDto> {

  /**
   * Converts a ProgrammeMembershipDTO to a CurriculumMembershipDmsDto.
   *
   * @param programmeMembershipDto the ProgrammeMembershipDTO to convert
   * @return the CurriculumMembershipDmsDto
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
  CurriculumMembershipDmsDto toDmsDto(ProgrammeMembershipDTO programmeMembershipDto);

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

  /**
   * Builds a list of DmsDtos from a programme membership's curriculum memberships.
   *
   * <p>Each will have details from the parent programme membership, as well as curriculum
   * membership-specific information.</p>
   *
   * @param pmDto the ProgrammeMembershipDTO to process
   * @return a list of CurriculumMembershipDmsDtos
   */
  @Override
  default List<CurriculumMembershipDmsDto> toListDmsDto(ProgrammeMembershipDTO pmDto) {
    List<CurriculumMembershipDmsDto> dmsDtos = new ArrayList<>();
    CurriculumMembershipDmsDto dmsDtoFromPm = toDmsDto(pmDto);
    CurriculumMembershipMapper curriculumMembershipMapper = new CurriculumMembershipMapperImpl();

    for (CurriculumMembershipDTO cm : pmDto.getCurriculumMemberships()) {
      CurriculumMembershipDmsDto dmsDto = curriculumMembershipMapper.toDmsDto(cm);
      if (dmsDto != null) {
        dmsDto.setProgrammeMembershipUuid(dmsDtoFromPm.getProgrammeMembershipUuid());
        dmsDto.setPersonId(dmsDtoFromPm.getPersonId());
        dmsDto.setProgrammeId(dmsDtoFromPm.getProgrammeId());
        dmsDto.setRotationId(dmsDtoFromPm.getRotationId());
        dmsDto.setRotation(dmsDtoFromPm.getRotation());
        dmsDto.setTrainingNumberId(dmsDtoFromPm.getTrainingNumberId());
        dmsDto.setTrainingPathway(dmsDtoFromPm.getTrainingPathway());
        dmsDto.setProgrammeMembershipType(dmsDtoFromPm.getProgrammeMembershipType());
        dmsDto.setProgrammeStartDate(dmsDtoFromPm.getProgrammeStartDate());
        dmsDto.setProgrammeEndDate(dmsDtoFromPm.getProgrammeEndDate());
        dmsDto.setLeavingReason(dmsDtoFromPm.getLeavingReason());
        dmsDto.setLeavingDestination(dmsDtoFromPm.getLeavingDestination());

        dmsDtos.add(dmsDto);
      }
    }
    return dmsDtos;
  }
}
