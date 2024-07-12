package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import uk.nhs.tis.sync.dto.HeeUserDmsDto;

/**
 * A mapper to map between Profile and DMS DTOs for the HeeUser data type.
 */
@Mapper(componentModel = "spring")
public interface HeeUserMapper extends DmsMapper<HeeUserDTO, HeeUserDmsDto> {

  //note that password, roles, etc. in the HeeUserDTO are excluded from HeeUserDmsDto: we simply
  //map the 'base' record fields
  @Mapping(target = "active", source = "active",
      qualifiedByName = "getBooleanAsZeroOrOne")
  HeeUserDmsDto toDmsDto(HeeUserDTO heeUserDto);

  @Named("getBooleanAsZeroOrOne")
  default String getBooleanAsZeroOrOne(boolean bool) {
    return bool ? "1" : "0";
  }
}
