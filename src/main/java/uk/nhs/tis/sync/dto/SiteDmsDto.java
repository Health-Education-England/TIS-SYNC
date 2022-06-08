package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
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
