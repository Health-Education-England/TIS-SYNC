package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.tis.sync.dto.ProgrammeDmsDto;
import uk.nhs.tis.sync.mapper.util.ProgrammeDmsDtoUtil;
import uk.nhs.tis.sync.mapper.util.ProgrammeDmsDtoUtil.Map;

@Mapper(componentModel = "spring", uses = ProgrammeDmsDtoUtil.class)
public interface ProgrammeMapper {

  @Mapping(target = "curriculaIds", source = "programmeDto.curricula", qualifiedBy = Map.class)
  ProgrammeDmsDto toDmsDto(ProgrammeDTO programmeDto);
}
