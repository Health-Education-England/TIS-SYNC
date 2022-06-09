package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.tis.sync.dto.SpecialtyDmsDto;

@Mapper(componentModel = "spring")
public interface SpecialtyMapper extends DmsMapper<SpecialtyDTO, SpecialtyDmsDto> {

  @Mapping(target = "specialtyGroupId", source = "specialtyGroup.id")
  SpecialtyDmsDto toDmsDto(SpecialtyDTO specialty);
}
