package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import org.mapstruct.Mapper;
import uk.nhs.tis.sync.dto.PersonDmsDto;

/**
 * A mapper to map between TCS and DMS DTOs for the Person data type.
 */
@Mapper(componentModel = "spring")
public interface PersonMapper extends DmsMapper<PersonDTO, PersonDmsDto> {

  PersonDmsDto toDmsDto(PersonDTO personDto);
}
