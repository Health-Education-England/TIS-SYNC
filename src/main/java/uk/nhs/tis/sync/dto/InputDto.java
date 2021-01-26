package uk.nhs.tis.sync.dto;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class InputDto {
  Object data;
  MetadataDto metadata;

  /**
   * And object that encapsulates the information to be sent into a Kinesis data stream.
   * @param data Normally a previously fetched dto (e.g. PostDTO)
   * @param metadata The metadata to be sent together with it (a Metadata object with three String fields)
   */
  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public InputDto(@JsonProperty("data")Object data, @JsonProperty("metadata") MetadataDto metadata) {
    this.data = data;
    this.metadata = metadata;
  }
}
