package uk.nhs.tis.sync.dto;

import java.util.UUID;
import lombok.Data;

/**
 * A DTO for transferring LocalOffice data to the DMS.
 */
@Data
public class LocalOfficeDmsDto {
  private String id;
  private UUID uuid;
  private String abbreviation;
  private String name;
  private String postAbbreviation;
  private String status;
}
