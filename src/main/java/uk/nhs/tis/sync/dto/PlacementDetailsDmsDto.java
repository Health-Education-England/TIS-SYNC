package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PlacementDetailsDmsDto {

  private String id;
  private String dateFrom;
  private String dateTo;
  private String wholeTimeEquivalent;
  private String intrepidId;
  private String traineeId;
  private String postId;
  private String gradeAbbreviation;
  private String placementType;
  private String status;
  private String trainingDescription;
  private String gradeId;
  private String lifecycleState;
  private String siteId;
  private String siteCode;
  private String localPostNumber;
}
