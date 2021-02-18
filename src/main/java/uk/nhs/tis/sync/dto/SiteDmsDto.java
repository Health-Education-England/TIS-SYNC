package uk.nhs.tis.sync.dto;

import lombok.Data;

@Data
public class SiteDmsDto {

  private String id;
  private String intrepidId;
  private String startDate;
  private String endDate;
  private String localOffice;
  private String organisationalUnit;
  private String trustId;
  private String trustCode;
  private String siteCode;
  private String siteNumber;
  private String siteName;
  private String siteKnownAs;
  private String address;
  private String postCode;
  private String status;
}
