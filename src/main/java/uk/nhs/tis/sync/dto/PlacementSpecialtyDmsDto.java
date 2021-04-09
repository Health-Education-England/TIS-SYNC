package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class PlacementSpecialtyDmsDto {

  private String placementId;
  private String specialtyId;
  private String placementSpecialtyType;
  private String specialtyName;

}
