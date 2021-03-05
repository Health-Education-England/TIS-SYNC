package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class SpecialtyDmsDto {

  private String id;
  private String status;
  private String college;
  private String specialtyCode;
  private String specialtyGroupId;
  private String intrepidId;
  private String name;

}
