package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.ProgrammeCurriculumDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeDTO;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.tis.sync.dto.ProgrammeDmsDto;

@Mapper(componentModel = "spring")
public interface ProgrammeMapper {

  @Mapping(target = "curriculaIds", source = "programmeDto.curricula")
  ProgrammeDmsDto toDmsDto(ProgrammeDTO programmeDto);

  default String programmeCurriculumToCurricumId(ProgrammeCurriculumDTO curriculumDTO) {
    return curriculumDTO.getCurriculum().getId().toString();
  }

  default Set<String> programmeCurriculaToCurricumIds(Set<ProgrammeCurriculumDTO> curriculumDtos) {
    return curriculumDtos.stream().map(this::programmeCurriculumToCurricumId)
        .collect(Collectors.toSet());
  }
}
