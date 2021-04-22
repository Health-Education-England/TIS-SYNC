package uk.nhs.tis.sync.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.PlacementDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.LifecycleState;
import com.transformuk.hee.tis.tcs.api.enumeration.PlacementStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import uk.nhs.tis.sync.dto.PlacementDmsDto;

public class PlacementMapperTest {

  PlacementMapper mapper;

  PlacementDTO placementDto;

  @Before
  public void setUp() {
    mapper = Mappers.getMapper(PlacementMapper.class);

    placementDto = new PlacementDTO();
    placementDto.setId(45L);
    placementDto.setDateFrom(LocalDate.MIN);
    placementDto.setDateTo(LocalDate.MAX);
    placementDto.setPlacementWholeTimeEquivalent(new BigDecimal("1"));
    placementDto.setIntrepidId("00");
    placementDto.setTraineeId(4500L);
    placementDto.setPostId(5L);
    placementDto.setGradeAbbreviation("gradeAbbreviation");
    placementDto.setPlacementType("placementType");
    placementDto.setStatus(PlacementStatus.CURRENT);
    placementDto.setTrainingDescription("trainingDescription");
    placementDto.setGradeId(20L);
    placementDto.setLifecycleState(LifecycleState.APPROVED);
    placementDto.setSiteId(30L);
    placementDto.setSiteCode("siteCode");
    placementDto.setLocalPostNumber("PO5TN0");
  }

  @Test
  public void shouldMapAPlacementDtoToADataDmsDto() {
    PlacementDmsDto placementDmsDto = mapper.toDmsDto(placementDto);

    assertEquals("45", placementDmsDto.getId());
    assertEquals(LocalDate.MIN.toString(), placementDmsDto.getDateFrom());
    assertEquals(LocalDate.MAX.toString(), placementDmsDto.getDateTo());
    assertEquals("1", placementDmsDto.getPlacementWholeTimeEquivalent());
    assertEquals("00", placementDmsDto.getIntrepidId());
    assertEquals("4500", placementDmsDto.getTraineeId());
    assertEquals("5", placementDmsDto.getPostId());
    assertEquals("gradeAbbreviation", placementDmsDto.getGradeAbbreviation());
    assertEquals("placementType", placementDmsDto.getPlacementType());
    assertEquals("CURRENT", placementDmsDto.getStatus());
    assertEquals("trainingDescription", placementDmsDto.getTrainingDescription());
    assertEquals("20", placementDmsDto.getGradeId());
    assertEquals("APPROVED", placementDmsDto.getLifecycleState());
    assertEquals("30", placementDmsDto.getSiteId());
    assertEquals("siteCode", placementDmsDto.getSiteCode());
    assertEquals("PO5TN0", placementDmsDto.getLocalPostNumber());
  }
}
