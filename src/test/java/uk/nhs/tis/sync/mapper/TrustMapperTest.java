package uk.nhs.tis.sync.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.api.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.nhs.tis.sync.dto.TrustDmsDto;

public class TrustMapperTest {

  private TrustMapper mapper;

  private TrustDTO trustDto;

  @BeforeEach
  public void setUp() {
    mapper = Mappers.getMapper(TrustMapper.class);

    trustDto = new TrustDTO();
    trustDto.setId(1L);
    trustDto.setCode("22");
    trustDto.setLocalOffice("localOffice");
    trustDto.setStatus(Status.CURRENT);
    trustDto.setTrustKnownAs("trustKnownAs");
    trustDto.setTrustName("trustName");
    trustDto.setTrustNumber("trustNumber");
    trustDto.setIntrepidId("intrepidId");
  }

  @Test
  public void shouldMapTrustDtoInATrustDataDmsDto() {
    TrustDmsDto trustDmsDto = mapper.toDmsDto(trustDto);

    assertEquals("22", trustDmsDto.getCode());
    assertEquals("1", trustDmsDto.getId());
    assertEquals("localOffice", trustDmsDto.getLocalOffice());
    assertEquals("CURRENT", trustDmsDto.getStatus());
    assertEquals("trustKnownAs", trustDmsDto.getTrustKnownAs());
    assertEquals("trustName", trustDmsDto.getTrustName());
    assertEquals("trustNumber", trustDmsDto.getTrustNumber());
    assertEquals("intrepidId", trustDmsDto.getIntrepidId());
  }
}
