package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class PlacementSpecialtyMessageDto {

  String table;
  String placementId;
  String placementSpecialtyType;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public PlacementSpecialtyMessageDto(@JsonProperty("table") String table,
      @JsonProperty("placementId") String placementId,
      @JsonProperty("placementSpecialtyType") String placementSpecialtyType) {
    this.table = table;
    this.placementId = placementId;
    this.placementSpecialtyType = placementSpecialtyType;
  }
}
