package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class AmazonSqsMessageDto {
  String table;
  String id;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public AmazonSqsMessageDto(@JsonProperty("table") String table, @JsonProperty("id") String id) {
    this.table = table;
    this.id = id;
  }
}
