package uk.nhs.tis.sync.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.profile.dto.RoleDTO;
import com.transformuk.hee.tis.profile.service.dto.HeeUserDTO;
import com.transformuk.hee.tis.profile.service.dto.UserProgrammeDTO;
import com.transformuk.hee.tis.profile.service.dto.UserTrustDTO;
import java.util.Collections;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.nhs.tis.sync.dto.HeeUserDmsDto;

class HeeUserMapperTest {

  private HeeUserMapper mapper;

  private HeeUserDTO heeUserDto;

  @BeforeEach
  public void setUp() {
    mapper = new HeeUserMapperImpl();

    heeUserDto = new HeeUserDTO();
    heeUserDto.setActive(true);
    heeUserDto.setAssociatedProgrammes(Collections.singleton(new UserProgrammeDTO()));
    heeUserDto.setAssociatedTrusts(Collections.singleton(new UserTrustDTO()));
    heeUserDto.setFirstName("first name");
    heeUserDto.setName("name");
    heeUserDto.setDesignatedBodyCodes(Collections.singleton("dbc1"));
    heeUserDto.setEmailAddress("email");
    heeUserDto.setGmcId("gmc");
    heeUserDto.setLastName("last name");
    heeUserDto.setPassword("password");
    heeUserDto.setPhoneNumber("phone");
    heeUserDto.setRoles(Collections.singleton(new RoleDTO()));
    heeUserDto.setTemporaryPassword(true);
  }

  @Test
  public void shouldMapAHeeUserDtoToADataDmsDto() {
    HeeUserDmsDto heeUserDmsDto = mapper.toDmsDto(heeUserDto);

    assertEquals("1", heeUserDmsDto.getActive());
    assertEquals("name", heeUserDmsDto.getName());
    assertEquals("first name", heeUserDmsDto.getFirstName());
    assertEquals("gmc", heeUserDmsDto.getGmcId());
    assertEquals("last name", heeUserDmsDto.getLastName());
    assertEquals("email", heeUserDmsDto.getEmailAddress());
    assertEquals("phone", heeUserDmsDto.getPhoneNumber());
    //other HeeUserDto properties are ignored
  }

  @ParameterizedTest
  @ValueSource(booleans = {false, true})
  void shouldMapBooleansToZeroOrOneString(boolean bool) {
    heeUserDto.setActive(bool);
    HeeUserDmsDto heeUserDmsDto = mapper.toDmsDto(heeUserDto);
    assertEquals(bool ? "1" : "0", heeUserDmsDto.getActive());
  }
}
