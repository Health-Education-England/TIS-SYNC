package uk.nhs.tis.sync.dto;

import lombok.Data;

@Data
public class HeeUserDmsDto {
  private final String name;
  private final String firstName;
  private final String lastName;
  private final String gmcId;
  private final String phoneNumber;
  private final String emailAddress;
  private final String active;
}
