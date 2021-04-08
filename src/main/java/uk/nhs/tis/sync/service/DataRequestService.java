package uk.nhs.tis.sync.service;

import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.tis.sync.dto.AmazonSqsMessageDto;
import uk.nhs.tis.sync.dto.PlacementSpecialtyMessageDto;

@Slf4j
@Service
public class DataRequestService {

  private static final String TABLE_CURRICULUM = "Curriculum";
  private static final String TABLE_POST = "Post";
  private static final String TABLE_PROGRAMME = "Programme";
  private static final String TABLE_SPECIALTY = "Specialty";
  private static final String TABLE_SITE = "Site";
  private static final String TABLE_TRUST = "Trust";

  private final TcsServiceImpl tcsServiceImpl;

  private final ReferenceServiceImpl referenceServiceImpl;

  DataRequestService(TcsServiceImpl tcsServiceImpl, ReferenceServiceImpl referenceServiceImpl) {
    this.tcsServiceImpl = tcsServiceImpl;
    this.referenceServiceImpl = referenceServiceImpl;
  }

  /**
   * Retrieve a DTO using TcsServiceImpl according to the info contained in an Amazon SQS message.
   *
   * @param amazonSqsMessageDto The amazonSqsMessageDto to get info from for DTO retrieval.
   */
  public Object retrieveDto(AmazonSqsMessageDto amazonSqsMessageDto) {
    try {
      long id = Long.parseLong(amazonSqsMessageDto.getId());

      switch (amazonSqsMessageDto.getTable()) {
        case TABLE_CURRICULUM:
          return tcsServiceImpl.getCurriculumById(id);
        case TABLE_POST:
          return tcsServiceImpl.getPostById(id);
        case TABLE_PROGRAMME:
          return tcsServiceImpl.findProgrammesIn(Collections.singletonList(String.valueOf(id)))
              .get(0);
        case TABLE_SITE:
          return referenceServiceImpl.findSitesIdIn(Collections.singleton(id)).get(0);
        case TABLE_SPECIALTY:
          return tcsServiceImpl.getSpecialtyById(id);
        case TABLE_TRUST:
          return referenceServiceImpl.findTrustById(id);
        default:
          break;
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return null;
  }

  public PlacementSpecialtyDTO retrievePlacementSpecialtyDto(PlacementSpecialtyMessageDto message) {
    long placementId = Long.parseLong(message.getPlacementId());

    PlacementDetailsDTO placementDetails = tcsServiceImpl.getPlacementById(placementId);
    return placementDetails.getSpecialties().stream()
        .filter(this::isPrimary)
        .findFirst()
        .get();
  }

  private boolean isPrimary(PlacementSpecialtyDTO placementSpecialty) {
    return placementSpecialty.getPlacementSpecialtyType() == PostSpecialtyType.PRIMARY;
  }
}
