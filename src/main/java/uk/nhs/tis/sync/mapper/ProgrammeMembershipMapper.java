package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.tis.sync.dto.ProgrammeMembershipDmsDto;
import uk.nhs.tis.sync.mapper.util.ProgrammeMembershipDmsDtoUtil;

import static uk.nhs.tis.sync.mapper.util.ProgrammeMembershipDmsDtoUtil.*;

@Mapper(componentModel = "spring", uses = ProgrammeMembershipDmsDtoUtil.class)
public interface ProgrammeMembershipMapper {

  @Mapping(target = "trainingNumberId", source = "programmeMembershipDto.trainingNumber.id")
  @Mapping(target = "rotationId", source = "programmeMembershipDto.rotation.id")
  @Mapping(target = "personId", source = "programmeMembershipDto.person.id")
  @Mapping(target = "curriculumMembershipIds",
      source = "programmeMembershipDto.curriculumMemberships", qualifiedBy = Map.class)
  ProgrammeMembershipDmsDto toDmsDto(ProgrammeMembershipDTO programmeMembershipDto);
}
