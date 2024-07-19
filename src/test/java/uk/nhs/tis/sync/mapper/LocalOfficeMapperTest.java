package uk.nhs.tis.sync.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.transformuk.hee.tis.reference.api.dto.LocalOfficeDTO;
import com.transformuk.hee.tis.reference.api.enums.Status;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.nhs.tis.sync.dto.LocalOfficeDmsDto;

class LocalOfficeMapperTest {

  private LocalOfficeMapper mapper;

  private LocalOfficeDTO localOfficeDto;

  private final UUID uuid = UUID.randomUUID();

  @BeforeEach
  public void setUp() {
    mapper = new LocalOfficeMapperImpl();

    localOfficeDto = new LocalOfficeDTO();
    localOfficeDto.setName("name");
    localOfficeDto.setAbbreviation("abbr");
    localOfficeDto.setId(1L);
    localOfficeDto.setUuid(uuid);
    localOfficeDto.setStatus(Status.CURRENT);
  }

  @Test
  void shouldMapALocalOfficeDtoToADataDmsDto() {
    LocalOfficeDmsDto localOfficeDmsDto = mapper.toDmsDto(localOfficeDto);

    assertEquals("1", localOfficeDmsDto.getId());
    assertEquals("name", localOfficeDmsDto.getName());
    assertEquals("abbr", localOfficeDmsDto.getAbbreviation());
    assertEquals(uuid, localOfficeDmsDto.getUuid());
    assertEquals("CURRENT", localOfficeDmsDto.getStatus());
  }

  @Test
  void shouldMapNullStatusToNull() {
    localOfficeDto.setStatus(null);
    LocalOfficeDmsDto localOfficeDmsDto = mapper.toDmsDto(localOfficeDto);

    assertNull(localOfficeDmsDto.getStatus());
  }
}
