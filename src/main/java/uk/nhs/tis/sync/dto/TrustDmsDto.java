package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TrustDmsDto {

  String code;
  String localOffice;
  String status;
  String trustKnownAs;
  String trustName;
  String trustNumber;
  String intrepidId;
  String id;
}
