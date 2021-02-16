package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"timestamp", "record-type", "operation", "partition-key-type", "schema-name",
    "table-name", "transaction-id"})
public class MetadataDto {
  @JsonProperty("timestamp") String timestamp;
  @JsonProperty("record-type") String recordType;
  @JsonProperty("operation") String operation;
  @JsonProperty("partition-key-type") String partitionKeyType;
  @JsonProperty("schema-name") String schemaName;
  @JsonProperty("table-name") String tableName;
  @JsonProperty("transaction-id") String transactionId;
}
