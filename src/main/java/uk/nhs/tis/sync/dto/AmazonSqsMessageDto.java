package uk.nhs.tis.sync.dto;

import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
@Value
public class AmazonSqsMessageDto {
  String table;
  String id;


}
