package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * A DmsDto for Qualifications.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QualificationDmsDto {
  private String id;
  private String qualification;
  private String qualificationType;
  private String qualificationAttainedDate;
  private String medicalSchool;
  private String countryOfQualification;
  private String personId;
  private String intrepidId;
  private String amendedDate;
}
