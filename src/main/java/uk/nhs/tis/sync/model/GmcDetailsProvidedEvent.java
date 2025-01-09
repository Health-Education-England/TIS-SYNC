package uk.nhs.tis.sync.model;

import com.transformuk.hee.tis.tcs.api.dto.GmcDetailsDTO;
import javax.validation.Valid;
import lombok.Getter;

/**
 * An event triggered when a trainee provides GMC details.
 */
@Getter
public class GmcDetailsProvidedEvent {

  private final Long personId;

  @Valid
  private final GmcDetailsDTO gmcDetails;

  public GmcDetailsProvidedEvent(Long personId, GmcDetailsDTO gmcDetails) {
    this.personId = personId;
    this.gmcDetails = gmcDetails;
  }
}
