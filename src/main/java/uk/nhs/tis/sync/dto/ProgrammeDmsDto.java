package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

import java.util.Set;

@Data
@JsonInclude(Include.NON_NULL)
public class ProgrammeDmsDto {

  private String id;
  private String intrepidId;
  private String status;
  private String owner;
  private String programmeName;
  private String programmeNumber;
  private Set<String> curriculaIds;
}
