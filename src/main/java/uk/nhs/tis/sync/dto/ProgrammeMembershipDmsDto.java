package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.transformuk.hee.tis.tcs.api.dto.*;
import com.transformuk.hee.tis.tcs.api.dto.validation.Create;
import com.transformuk.hee.tis.tcs.api.dto.validation.Update;
import com.transformuk.hee.tis.tcs.api.enumeration.ProgrammeMembershipType;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Data
@JsonInclude(Include.NON_NULL)
public class ProgrammeMembershipDmsDto {

  private String id;
  private String programmeMembershipType;
  private String personId;
  private String rotationId;
  private String programmeStartDate;
  private String programmeEndDate;
  private String leavingDestination;
  private String leavingReason;
  private String programmeId;
  private String programmeName;
  private String programmeNumber;
  private String trainingPathway;
  private String trainingNumberId;
  private List<String> curriculumMembershipIds;
}
