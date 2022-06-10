package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurriculumMembershipDmsDto {
  //CurriculumMembership details
  private String id;
  private String curriculumStartDate;
  private String curriculumEndDate;
  private String curriculumCompletionDate;
  private String periodOfGrace;
  private String curriculumId;
  private String intrepidId;
  private String programmeMembershipUuid;
  private String amendedDate;

  //ProgrammeMembership details
  private String personId;
  private String programmeId;
  private String rotationId;
  private String rotation;
  private String trainingNumberId;
  private String trainingPathway;
  private String programmeMembershipType;
  private String programmeStartDate;
  private String programmeEndDate;
  private String leavingReason;
  private String leavingDestination;
}
