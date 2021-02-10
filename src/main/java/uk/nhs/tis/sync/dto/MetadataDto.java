package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"timestamp", "record-type", "operation", "partition-key-type", "schema-name",
    "table-name", "transaction-id"})
@Data
public class MetadataDto {
  @JsonProperty("timestamp")
  String timestamp;
  @JsonProperty("record-type")
  String recordType;
  @JsonProperty("operation")
  String operation;
  @JsonProperty("partition-key-type")
  String partitionKeyType;
  @JsonProperty("schema-name")
  String schemaName;
  @JsonProperty("table-name")
  String tableName;
  @JsonProperty("transaction-id")
  String transactionId;

  /**
   * Metadata to be sent together with Data as part of a comprehensive object.
   * @param timestamp        The time the record got generated
   * @param recordType       The type or record (e.g. "data")
   * @param operation        The type of operation (e.g. "load")
   * @param partitionKeyType E.g. ("schema-table")
   * @param schemaName       The schema of the data (e.g. "tcs")
   * @param tableName        The table data belongs to
   * @param transactionId    The id of the transaction
   */
  public MetadataDto(String timestamp,
                     String recordType,
                     String operation,
                     String partitionKeyType,
                     String schemaName,
                     String tableName,
                     String transactionId) {
    this.timestamp = timestamp;
    this.recordType = recordType;
    this.operation = operation;
    this.partitionKeyType = partitionKeyType;
    this.schemaName = schemaName;
    this.tableName = tableName;
    this.transactionId = transactionId;
  }
}
