package uk.nhs.tis.sync.dto;

import lombok.Data;

/**
 * A DTO representing the expected structure for Person via DMS.
 */
@Data
public class PersonDmsDto {

  private final String id;
  private final String intrepidId;
  private final String addedDate;
  private final String amendedDate;
  private final String role;
  private final String status;
  private final String comments;
  private final String inactiveDate;
  private final String inactiveNotes;
  private final String publicHealthNumber;
  private final String regulator;
}
