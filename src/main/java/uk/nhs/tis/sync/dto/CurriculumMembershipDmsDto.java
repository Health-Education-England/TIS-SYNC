package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurriculumMembershipDmsDto {
  private String id;
  private String curriculumStartDate;
  private String curriculumEndDate;
  private String curriculumCompletionDate;
  private String periodOfGrace;
  private String curriculumId;
  private String intrepidId;
  private String programmeMembershipUuid;
  private String amendedDate;
}
