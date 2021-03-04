package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class CurriculumDmsDto {

  private String id;
  private String name;
  private String curriculumSubType;
  private String assessmentType;
  private String doesThisCurriculumLeadToCct;
  private String periodOfGrace;
  private String intrepidId;
  private String specialtyId;
  private String status;
  private String length;

}
