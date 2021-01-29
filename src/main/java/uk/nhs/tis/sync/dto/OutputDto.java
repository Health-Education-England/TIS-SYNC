package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class OutputDto {
  Object data;
  MetadataDto metadata;

  /**
   * And object that encapsulates the information to be sent into a Kinesis data stream.
   * @param data Normally a previously fetched dto (e.g. PostDTO)
   * @param metadata The Metadata object with three String fields to be sent in addition.
   */
  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public OutputDto(@JsonProperty("data")Object data,
                   @JsonProperty("metadata") MetadataDto metadata) {
    this.data = data;
    this.metadata = metadata;
  }
}