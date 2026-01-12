package uk.nhs.tis.sync.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.PlacementSummaryDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PlacementStatus;
import java.math.BigDecimal;
import java.text.ParseException;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import uk.nhs.tis.sync.dto.PlacementSummaryDmsDto;

public class PlacementMapperTest {

  PlacementSummaryMapper mapper;

  PlacementSummaryDTO placementSummaryDto;

  @BeforeEach
  public void setUp() throws ParseException {
    mapper = Mappers.getMapper(PlacementSummaryMapper.class);

    placementSummaryDto = new PlacementSummaryDTO();
    placementSummaryDto.setPlacementId(45L);
    placementSummaryDto.setDateFrom(DateUtils.parseDate("2020-01-01", "yyyy-MM-dd"));
    placementSummaryDto.setDateTo(DateUtils.parseDate("2022-02-02", "yyyy-MM-dd"));
    placementSummaryDto.setPlacementWholeTimeEquivalent(new BigDecimal("1"));
    placementSummaryDto.setTraineeId(4500L);
    placementSummaryDto.setPostId(5L);
    placementSummaryDto.setGradeAbbreviation("gradeAbbreviation");
    placementSummaryDto.setPlacementType("placementType");
    placementSummaryDto.setStatus(PlacementStatus.CURRENT.toString());
    placementSummaryDto.setGradeId(20L);
    placementSummaryDto.setSiteId(30L);
    //following are not used
    placementSummaryDto.setGradeName("grade name");
    placementSummaryDto.setSiteName("site name");
    placementSummaryDto.setPlacementStatus("calculated field");
  }

  @Test
  public void shouldMapAPlacementDetailsDtoToADataDmsDto() {
    PlacementSummaryDmsDto placementSummaryDmsDto = mapper.toDmsDto(placementSummaryDto);

    assertEquals("45", placementSummaryDmsDto.getId());
    assertEquals("2020-01-01", placementSummaryDmsDto.getDateFrom());
    assertEquals("2022-02-02", placementSummaryDmsDto.getDateTo());
    assertEquals("1", placementSummaryDmsDto.getWholeTimeEquivalent());
    assertEquals("4500", placementSummaryDmsDto.getTraineeId());
    assertEquals("5", placementSummaryDmsDto.getPostId());
    assertEquals("gradeAbbreviation", placementSummaryDmsDto.getGradeAbbreviation());
    assertEquals("placementType", placementSummaryDmsDto.getPlacementType());
    assertEquals("CURRENT", placementSummaryDmsDto.getStatus());
    assertEquals("20", placementSummaryDmsDto.getGradeId());
    assertEquals("30", placementSummaryDmsDto.getSiteId());
  }
}
