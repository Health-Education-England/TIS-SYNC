package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

/**
 * A DTO for transferring Programme Membership data via Amazon DMS.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProgrammeMembershipDmsDto {

  private UUID uuid;
  private String personId;
  private String programmeId;
  private String rotationId;
  private String rotation;
  private String trainingNumberId;
  private String programmeMembershipType;
  private LocalDate programmeStartDate;
  private LocalDate programmeEndDate;
  private String leavingReason;
  private String trainingPathway;
  private Instant amendedDate;
  private String leavingDestination;
}
