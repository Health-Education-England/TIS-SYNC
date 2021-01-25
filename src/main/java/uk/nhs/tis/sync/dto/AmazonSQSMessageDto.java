package uk.nhs.tis.sync.dto;

import lombok.Value;

@Value
public class AmazonSQSMessageDto {
  String table;
  String id;
}
