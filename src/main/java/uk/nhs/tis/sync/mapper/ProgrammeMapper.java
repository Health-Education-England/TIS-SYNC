package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import org.mapstruct.Mapper;
import uk.nhs.tis.sync.dto.ProgrammeDmsDto;

@Mapper(componentModel = "spring")
public interface ProgrammeMapper {

  ProgrammeDmsDto toDmsDto(ProgrammeDTO programmeDto);
}
