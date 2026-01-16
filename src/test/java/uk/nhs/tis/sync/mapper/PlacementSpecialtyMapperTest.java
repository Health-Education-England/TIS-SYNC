package uk.nhs.tis.sync.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import org.junit.jupiter.api.Test;
import uk.nhs.tis.sync.dto.PlacementSpecialtyDmsDto;

public class PlacementSpecialtyMapperTest {

  private static final Long PLACEMENT_ID = 50L;
  private static final Long SPECIALTY_ID = 60L;
  private static final String SPECIALTY_NAME = "specialtyName";
  private static final PostSpecialtyType PLACEMENT_SPECIALTY_TYPE = PostSpecialtyType.PRIMARY;
  private final PlacementSpecialtyMapper testObj = new PlacementSpecialtyMapperImpl();

  @Test
  public void toDmsDtoShouldMapSpecialty() {
    PlacementSpecialtyDTO input = new PlacementSpecialtyDTO();
    input.setSpecialtyId(SPECIALTY_ID);
    input.setPlacementId(PLACEMENT_ID);
    input.setPlacementSpecialtyType(PLACEMENT_SPECIALTY_TYPE);
    input.setSpecialtyName(SPECIALTY_NAME);

    PlacementSpecialtyDmsDto expected = testObj.toDmsDto(input);

    assertEquals(PLACEMENT_ID.toString(), expected.getPlacementId(),
        "Mapped placement id doesn't match expectation");
    assertEquals(SPECIALTY_ID, input.getSpecialtyId(),
        "Mapped specialty id doesn't match expectation");
    assertEquals(PLACEMENT_SPECIALTY_TYPE.name(),
        expected.getPlacementSpecialtyType(), "Mapped placement-specialty type doesn't match expectation");
    assertEquals(SPECIALTY_NAME, expected.getSpecialtyName(),
        "Mapped specialty name doesn't match expectation");
  }
}
