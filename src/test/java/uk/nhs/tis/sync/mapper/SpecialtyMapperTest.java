package uk.nhs.tis.sync.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyGroupDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.jupiter.api.Test;
import uk.nhs.tis.sync.dto.SpecialtyDmsDto;

public class SpecialtyMapperTest {

  private static final Long ID = 50L;
  private static final String LEGACY_ID = "i50";
  private static final String COLLEGE = "college";
  private static final String SPECIALTY_CODE = "111";
  private static final Status STATUS = Status.CURRENT;
  private static final String NAME = "name";
  private final SpecialtyMapper testObj = new SpecialtyMapperImpl();

  @Test
  public void toDmsDtoShouldMapSpecialty() {
    SpecialtyDTO input = new SpecialtyDTO();
    input.setId(ID);
    input.setStatus(STATUS);
    input.setIntrepidId(LEGACY_ID);
    input.setCollege(COLLEGE);
    input.setSpecialtyCode(SPECIALTY_CODE);
    input.setName(NAME);

    SpecialtyGroupDTO specialtyGroup = new SpecialtyGroupDTO();
    specialtyGroup.setId(3L);
    input.setSpecialtyGroup(specialtyGroup);

    SpecialtyDmsDto expected = testObj.toDmsDto(input);

    assertEquals(ID.toString(), expected.getId(), "Mapped id doesn't match expectation");
    assertEquals(LEGACY_ID, input.getIntrepidId(),
        "Mapped intrepid id doesn't match expectation");
    assertEquals(STATUS.name(), expected.getStatus(), "Mapped status doesn't match expectation");
    assertEquals(COLLEGE, expected.getCollege(), "Mapped college doesn't match expectation");
    assertEquals(SPECIALTY_CODE, expected.getSpecialtyCode(),
        "Mapped specialty code doesn't match expectation");
    assertEquals(input.getSpecialtyGroup().getId().toString(),
        expected.getSpecialtyGroupId(), "Mapped specialty group id doesn't match expectation");
    assertEquals(NAME, expected.getName(), "Mapped name doesn't match expectation");
  }
}
