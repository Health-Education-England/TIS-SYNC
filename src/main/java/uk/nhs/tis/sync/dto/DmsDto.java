package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DmsDto {
  Object data;
  MetadataDto metadata;
}
