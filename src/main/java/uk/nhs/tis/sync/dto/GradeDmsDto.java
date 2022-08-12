package uk.nhs.tis.sync.dto;

import lombok.Data;

/**
 * A DTO representing the expected structure for Grade via DMS.
 */
@Data
public class GradeDmsDto {

  private final String id;
  private final String abbreviation;
  private final String label;
  private final String status;
  private final String name;
  private final String placementGrade;
  private final String postGrade;
  private final String trainingGrade;
  private final String intrepidId;
}
