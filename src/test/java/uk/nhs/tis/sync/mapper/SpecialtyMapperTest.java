package uk.nhs.tis.sync.mapper;

import static org.junit.Assert.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyGroupDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.Test;
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

    assertEquals("Mapped id doesn't match expectation", ID.toString(), expected.getId());
    assertEquals("Mapped intrepid id doesn't match expectation", LEGACY_ID,
        input.getIntrepidId());
    assertEquals("Mapped status doesn't match expectation", STATUS.name(), expected.getStatus());
    assertEquals("Mapped college doesn't match expectation", COLLEGE, expected.getCollege());
    assertEquals("Mapped specialty code doesn't match expectation", SPECIALTY_CODE,
        expected.getSpecialtyCode());
    assertEquals("Mapped specialty group id doesn't match expectation",
        input.getSpecialtyGroup().getId().toString(), expected.getSpecialtyGroupId());
    assertEquals("Mapped name doesn't match expectation", NAME, expected.getName());
  }
}
