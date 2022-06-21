package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PlacementSummaryDmsDto {

  private String id;
  private String dateFrom;
  private String dateTo;
  private String wholeTimeEquivalent;
  private String traineeId;
  private String postId;
  private String gradeAbbreviation;
  private String placementType;
  private String status;
  private String gradeId;
  private String siteId;
}
