package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class TrustDataDmsDto {

  String code;
  String localOffice;
  String status;
  String trustKnownAs;
  String trustName;
  String trustNumber;
  String intrepidId;
  String id;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public TrustDataDmsDto(@JsonProperty("code") String code,
                         @JsonProperty("localOffice") String localOffice,
                         @JsonProperty("status") String status,
                         @JsonProperty("trustKnownAs") String trustKnownAs,
                         @JsonProperty("trustName") String trustName,
                         @JsonProperty("trustNumber") String trustNumber,
                         @JsonProperty("intrepidId") String intrepidId,
                         @JsonProperty("id") String id) {
    this.code = code;
    this.localOffice = localOffice;
    this.status = status;
    this.trustKnownAs = trustKnownAs;
    this.trustName = trustName;
    this.trustNumber = trustNumber;
    this.intrepidId = intrepidId;
    this.id = id;
  }
}
