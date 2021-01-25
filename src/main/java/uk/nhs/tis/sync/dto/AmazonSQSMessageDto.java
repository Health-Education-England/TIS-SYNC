package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class AmazonSQSMessageDto {
  String table;
  String id;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public AmazonSQSMessageDto(@JsonProperty("table") String table, @JsonProperty("id") String id) {
    this.table = table;
    this.id = id;
  }
}
