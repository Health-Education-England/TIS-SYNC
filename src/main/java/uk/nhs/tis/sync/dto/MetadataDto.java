package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class MetadataDto {
  String schema;
  String table;
  String operation;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public MetadataDto(@JsonProperty("schema") String schema,
                     @JsonProperty("table") String table,
                     @JsonProperty("operation") String operation) {
    this.schema = schema;
    this.table = table;
    this.operation = operation;
  }
}
