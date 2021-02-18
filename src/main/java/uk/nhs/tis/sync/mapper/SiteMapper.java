package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.reference.api.dto.SiteDTO;
import org.mapstruct.Mapper;
import uk.nhs.tis.sync.dto.SiteDmsDto;

@Mapper(componentModel = "spring")
public interface SiteMapper {

  SiteDmsDto toDmsDto(SiteDTO site);
}
