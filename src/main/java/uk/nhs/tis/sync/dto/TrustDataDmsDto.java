package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TrustDataDmsDto {

  @JsonProperty("code") String code;
  @JsonProperty("localOffice") String localOffice;
  @JsonProperty("status") String status;
  @JsonProperty("trustKnownAs") String trustKnownAs;
  @JsonProperty("trustName") String trustName;
  @JsonProperty("trustNumber") String trustNumber;
  @JsonProperty("intrepidId") String intrepidId;
  @JsonProperty("id") String id;

  /**
   * Constructor for a TrustDataDmsDto, an object which represents the "data" portion of a record.
   * A DmsDto will have it as a field along with a MetadataDto. It's meant to reflect only the
   * relevant fields of a TrustDto.
   */
  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public TrustDataDmsDto() {
    //Constructor for a TrustDataDmsDto
  }
}
