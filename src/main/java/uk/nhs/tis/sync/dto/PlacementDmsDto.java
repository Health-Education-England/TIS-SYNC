package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.transformuk.hee.tis.tcs.api.dto.PlacementCommentDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSiteDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSupervisorDTO;
import com.transformuk.hee.tis.tcs.api.dto.validation.Create;
import com.transformuk.hee.tis.tcs.api.dto.validation.Update;
import com.transformuk.hee.tis.tcs.api.enumeration.LifecycleState;
import com.transformuk.hee.tis.tcs.api.enumeration.PlacementStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PlacementDmsDto {

  private String id;
  private String dateFrom;
  private String dateTo;
  private String placementWholeTimeEquivalent;
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
