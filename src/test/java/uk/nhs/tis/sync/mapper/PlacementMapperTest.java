package uk.nhs.tis.sync.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.LifecycleState;
import com.transformuk.hee.tis.tcs.api.enumeration.PlacementStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import uk.nhs.tis.sync.dto.PlacementDetailsDmsDto;

public class PlacementMapperTest {

  PlacementDetailsMapper mapper;

  PlacementDetailsDTO placementDetailsDto;

  @Before
  public void setUp() {
    mapper = Mappers.getMapper(PlacementDetailsMapper.class);

    placementDetailsDto = new PlacementDetailsDTO();
    placementDetailsDto.setId(45L);
    placementDetailsDto.setDateFrom(LocalDate.MIN);
    placementDetailsDto.setDateTo(LocalDate.MAX);
    placementDetailsDto.setWholeTimeEquivalent(new BigDecimal("1"));
    placementDetailsDto.setIntrepidId("00");
    placementDetailsDto.setTraineeId(4500L);
    placementDetailsDto.setPostId(5L);
    placementDetailsDto.setGradeAbbreviation("gradeAbbreviation");
    placementDetailsDto.setPlacementType("placementType");
    placementDetailsDto.setStatus(PlacementStatus.CURRENT);
    placementDetailsDto.setTrainingDescription("trainingDescription");
    placementDetailsDto.setGradeId(20L);
    placementDetailsDto.setLifecycleState(LifecycleState.APPROVED);
    placementDetailsDto.setSiteId(30L);
    placementDetailsDto.setSiteCode("siteCode");
    placementDetailsDto.setLocalPostNumber("PO5TN0");
  }

  @Test
  public void shouldMapAPlacementDetailsDtoToADataDmsDto() {
    PlacementDetailsDmsDto placementDetailsDmsDto = mapper.toDmsDto(placementDetailsDto);

    assertEquals("45", placementDetailsDmsDto.getId());
    assertEquals(LocalDate.MIN.toString(), placementDetailsDmsDto.getDateFrom());
    assertEquals(LocalDate.MAX.toString(), placementDetailsDmsDto.getDateTo());
    assertEquals("1", placementDetailsDmsDto.getWholeTimeEquivalent());
    assertEquals("00", placementDetailsDmsDto.getIntrepidId());
    assertEquals("4500", placementDetailsDmsDto.getTraineeId());
    assertEquals("5", placementDetailsDmsDto.getPostId());
    assertEquals("gradeAbbreviation", placementDetailsDmsDto.getGradeAbbreviation());
    assertEquals("placementType", placementDetailsDmsDto.getPlacementType());
    assertEquals("CURRENT", placementDetailsDmsDto.getStatus());
    assertEquals("trainingDescription", placementDetailsDmsDto.getTrainingDescription());
    assertEquals("20", placementDetailsDmsDto.getGradeId());
    assertEquals("APPROVED", placementDetailsDmsDto.getLifecycleState());
    assertEquals("30", placementDetailsDmsDto.getSiteId());
    assertEquals("siteCode", placementDetailsDmsDto.getSiteCode());
    assertEquals("PO5TN0", placementDetailsDmsDto.getLocalPostNumber());
  }
}
