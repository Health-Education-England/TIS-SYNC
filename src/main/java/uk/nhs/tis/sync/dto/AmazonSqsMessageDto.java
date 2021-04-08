package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.Nullable;
import lombok.Value;

@Value
public class AmazonSqsMessageDto {

  String table;
  String id;
  String placementId;
  String placementSpecialtyType;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public AmazonSqsMessageDto(@JsonProperty("table") String table, @JsonProperty("id") String id,
      @JsonProperty("placementId") String placementId, @JsonProperty("placementSpecialtyType")
      String placementSpecialtyType) {
    this.table = table;
    this.id = id;
    this.placementId = placementId;
    this.placementSpecialtyType = placementSpecialtyType;
  }

}
