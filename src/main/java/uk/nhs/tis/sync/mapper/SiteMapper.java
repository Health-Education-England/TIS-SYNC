package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import org.mapstruct.Mapper;
import uk.nhs.tis.sync.dto.SiteDmsDto;

@Mapper(componentModel = "spring")
public interface SiteMapper extends DmsMapper<SiteDTO, SiteDmsDto> {

  SiteDmsDto toDmsDto(SiteDTO site);
}
