package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.reference.api.dto.LocalOfficeDTO;
import com.transformuk.hee.tis.reference.api.enums.Status;
import org.mapstruct.Mapper;
import uk.nhs.tis.sync.dto.LocalOfficeDmsDto;

@Mapper(componentModel = "spring")
public interface LocalOfficeMapper extends DmsMapper<LocalOfficeDTO, LocalOfficeDmsDto> {

  LocalOfficeDmsDto toDmsDto(LocalOfficeDTO localOfficeDto);

  /**
   * Maps a Status to string.
   *
   * @param source The status to map.
   * @return The string value of the status.
   */
  default String map(Status source) {
    return source == null ? null : source.toString();
  }
}
