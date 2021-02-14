package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.reference.api.enums.Status;
import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.util.ReflectionUtils;
import uk.nhs.tis.sync.dto.TrustDataDmsDto;
import uk.nhs.tis.sync.mapper.util.TrustDataDmsDtoUtil;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TrustDtoToTrustDataDmsDtoMapperTest {

  private TrustDtoToTrustDataDmsDtoMapper mapper;

  private TrustDTO trustDto;

  @Before
  public void setUp() {
    mapper = Mappers.getMapper(TrustDtoToTrustDataDmsDtoMapper.class);
    Field field = ReflectionUtils.findField(TrustDtoToTrustDataDmsDtoMapperImpl.class,
        "trustDataDmsDtoUtil");
    field.setAccessible(true);
    ReflectionUtils.setField(field, mapper, new TrustDataDmsDtoUtil());

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
    TrustDataDmsDto trustDataDmsDto = mapper.trustDtoToTrustDataDmsDto(trustDto);

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
