package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.tis.sync.dto.ProgrammeMembershipDmsDto;

@Mapper(componentModel = "spring")
public interface ProgrammeMembershipMapper extends
    DmsMapper<ProgrammeMembershipDTO, ProgrammeMembershipDmsDto> {

  /**
   * Converts a ProgrammeMembershipDTO to a ProgrammeMembershipDmsDto.
   *
   * @param programmeMembershipDto the ProgrammeMembershipDTO to convert
   * @return the ProgrammeMembershipDmsDto
   */
  @Mapping(target = "uuid")
  @Mapping(target = "personId", source = "person.id")
  @Mapping(target = "programmeId")
  @Mapping(target = "rotationId", source = "rotation.id")
  @Mapping(target = "rotation", source = "rotation.name")
  @Mapping(target = "trainingNumberId", source = "trainingNumber.id")
  @Mapping(target = "programmeMembershipType")
  @Mapping(target = "programmeStartDate")
  @Mapping(target = "programmeEndDate")
  @Mapping(target = "leavingReason")
  @Mapping(target = "trainingPathway")
  @Mapping(target = "amendedDate")
  @Mapping(target = "leavingDestination")
  ProgrammeMembershipDmsDto toDmsDto(ProgrammeMembershipDTO programmeMembershipDto);

  /**
   * Maps a ProgrammeMembershipType to string.
   *
   * @param source The programme membership type to map.
   * @return The string value of the programme membership type.
   */
  default String map(ProgrammeMembershipType source) {
    return source == null ? null : source.toString();
  }
}
