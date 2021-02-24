package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public class ProgrammeDmsDto {

  private String id;
  private String status;
  private String owner;
  private String programmeName;
  private String programmeNumber;
  private String intrepidId;
}
