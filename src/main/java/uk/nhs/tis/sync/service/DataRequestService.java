package uk.nhs.tis.sync.service;

import static com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType.PRIMARY;

import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.nhs.tis.sync.dto.AmazonSqsMessageDto;

@Slf4j
@Service
public class DataRequestService {

  private static final String TABLE_CURRICULUM = "Curriculum";
  private static final String TABLE_PLACEMENT_SPECIALTY = "PlacementSpecialty";
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
      String table = amazonSqsMessageDto.getTable();

      switch (table) {
        case TABLE_CURRICULUM:
          return tcsServiceImpl.getCurriculumById(requestedId(amazonSqsMessageDto));
        case TABLE_PLACEMENT_SPECIALTY:
          return getPrimaryPlacementSpecialty(requestedPlacementId(amazonSqsMessageDto)).orElse(null);
        case TABLE_POST:
          return tcsServiceImpl.getPostById(requestedId(amazonSqsMessageDto));
        case TABLE_PROGRAMME:
          return tcsServiceImpl.findProgrammesIn(
              Collections.singletonList(String.valueOf(requestedId(amazonSqsMessageDto))))
              .get(0);
        case TABLE_SITE:
          return referenceServiceImpl
              .findSitesIdIn(Collections.singleton(requestedId(amazonSqsMessageDto))).get(0);
        case TABLE_SPECIALTY:
          return tcsServiceImpl.getSpecialtyById(requestedId(amazonSqsMessageDto));
        case TABLE_TRUST:
          return referenceServiceImpl.findTrustById(requestedId(amazonSqsMessageDto));
        default:
          break;

      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return null;
  }

  private Optional<PlacementSpecialtyDTO> getPrimaryPlacementSpecialty(String placementId) {
    PlacementDetailsDTO placement = tcsServiceImpl.getPlacementById(Long.parseLong(placementId));
    return placement.getSpecialties().stream()
        .filter(this::isPrimary)
        .findFirst();
  }

  private boolean isPrimary(PlacementSpecialtyDTO placementSpecialtyDto) {
    return placementSpecialtyDto.getPlacementSpecialtyType() == PRIMARY;
  }

  private long requestedId(AmazonSqsMessageDto amazonSqsMessageDto) {
    return Long.parseLong(amazonSqsMessageDto.getId());
  }

  private String requestedPlacementId(AmazonSqsMessageDto amazonSqsMessageDto) {
    return amazonSqsMessageDto.getPlacementId();
  }
}
