package uk.nhs.tis.sync.mapper;

import static org.junit.Assert.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.SpecialtyGroupDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.Test;
import uk.nhs.tis.sync.dto.PlacementSpecialtyDmsDto;
import uk.nhs.tis.sync.dto.SpecialtyDmsDto;

public class PlacementSpecialtyTest {


  private static final long PLACEMENT_ID = 50L;
  private static final long SPECIALTY_ID = 60L;
  private static final PostSpecialtyType PLACEMENT_SPECIALTY_TYPE = PostSpecialtyType.PRIMARY;
  private static final String SPECIALTY_NAME = "name";
  private final PlacementSpecialtyMapper testObj = new PlacementSpecialtyMapperImpl();

  @Test
  public void toDmsDtoShouldMapPlacementSpecialty() {
    PlacementSpecialtyDTO input = new PlacementSpecialtyDTO();
    input.setPlacementId(PLACEMENT_ID);
    input.setSpecialtyId(SPECIALTY_ID);
    input.setPlacementSpecialtyType(PLACEMENT_SPECIALTY_TYPE);
    input.setSpecialtyName(SPECIALTY_NAME);

    PlacementSpecialtyDmsDto expected = testObj.toDmsDto(input);

    assertEquals("Mapped placement id doesn't match expectation", String.valueOf(PLACEMENT_ID), expected.getPlacementId());
    assertEquals("Mapped specialty id id doesn't match expectation", String.valueOf(SPECIALTY_ID),
        expected.getSpecialtyId());
    assertEquals("Mapped placementSpecialty type doesn't match expectation", PLACEMENT_SPECIALTY_TYPE.name(), expected.getPlacementSpecialtyType());
    //assertEquals("Mapped specialty name doesn't match expectation", SPECIALTY_NAME, expected.getSpecialtyName());
  }
}
