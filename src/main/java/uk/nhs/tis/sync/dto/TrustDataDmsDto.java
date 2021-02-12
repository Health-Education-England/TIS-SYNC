package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Value;

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
   * @param code         Same code as in a TrustDto, but in String form
   * @param localOffice  Same localOffice as in a TrustDto
   * @param status       The String value of the status in a TrustDto (e.g. "CURRENT")
   * @param trustKnownAs Same trustKnownAs as in a TrustDto
   * @param trustName    Same trustName as in a TrustDto
   * @param trustNumber  Same trustNumber as in a TrustDto
   * @param intrepidId   Same intrepidId as in a TrustDto, but in String form
   * @param id           Same id as in a TrustDto, but in String form
   */
  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public TrustDataDmsDto() {}
}
