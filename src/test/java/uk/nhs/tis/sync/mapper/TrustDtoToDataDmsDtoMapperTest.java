package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.api.enums.Status;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import uk.nhs.tis.sync.dto.TrustDataDmsDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrustDtoToDataDmsDtoMapperTest {

  private TrustDtoToDataDmsDtoMapper mapper;

  private TrustDTO trustDto;

  @Before
  public void setUp() {
    mapper = new TrustDtoToDataDmsDtoMapper();

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
    TrustDataDmsDto trustDataDmsDto = mapper.trustDtoToDataDmsDto(trustDto);

    assertEquals("22", trustDataDmsDto.getCode());
    assertEquals("1", trustDataDmsDto.getId());
    assertEquals("localOffice", trustDataDmsDto.getLocalOffice());
    assertEquals("CURRENT", trustDataDmsDto.getStatus());
    assertEquals("trustKnownAs", trustDataDmsDto.getTrustKnownAs());
    assertEquals("trustName", trustDataDmsDto.getTrustName());
    assertEquals("trustNumber", trustDataDmsDto.getTrustNumber());
    assertEquals("intrepidId", trustDataDmsDto.getIntrepidId());
  }

}
