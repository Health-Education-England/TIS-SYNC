package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.CurriculumDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.tis.sync.dto.CurriculumDmsDto;

@Mapper(componentModel = "spring")
public interface CurriculumMapper {

  @Mapping(target = "specialtyId", source = "curriculumDto.specialty.id")
  CurriculumDmsDto toDmsDto(CurriculumDTO curriculumDto);
}
