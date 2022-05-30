package uk.nhs.tis.sync.service;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DataRequestService {

  private static final String TABLE_CURRICULUM = "Curriculum";
  private static final String TABLE_PLACEMENT = "Placement";
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
   * @param message The message to get info from for DTO retrieval.
   */
  public List<Object> retrieveDtos(Map<String, String> message) {
    try {
      String table = message.get("table");

      if (table.equals(TABLE_PLACEMENT_SPECIALTY) && message.containsKey("placementId")) {
        long placementId = Long.parseLong(message.get("placementId"));
        return createNonNullList(retrievePlacementSpecialty(placementId));
      }

      if (message.containsKey("id")) {
        long id = Long.parseLong(message.get("id"));

        switch (table) {
          case TABLE_CURRICULUM:
            return createNonNullList(tcsServiceImpl.getCurriculumById(id));
          case TABLE_PLACEMENT:
            return createNonNullList(tcsServiceImpl.getPlacementById(id));
          case TABLE_POST:
            return createNonNullList(tcsServiceImpl.getPostById(id));
          case TABLE_PROGRAMME:
            return createNonNullList(
                tcsServiceImpl.findProgrammesIn(singletonList(String.valueOf(id))).get(0));
          case TABLE_SITE:
            return createNonNullList(
                referenceServiceImpl.findSitesIdIn(Collections.singleton(id)).get(0));
          case TABLE_SPECIALTY:
            return createNonNullList(tcsServiceImpl.getSpecialtyById(id));
          case TABLE_TRUST:
            return createNonNullList(referenceServiceImpl.findTrustById(id));
          default:
            break;
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return emptyList();
  }

  private PlacementSpecialtyDTO retrievePlacementSpecialty(long placementId) {
    PlacementDetailsDTO placement = tcsServiceImpl.getPlacementById(placementId);
    Optional<PlacementSpecialtyDTO> placementSpecialtyDto = placement.getSpecialties().stream()
        .filter(ps -> ps.getPlacementSpecialtyType() == PostSpecialtyType.PRIMARY)
        .findFirst();
    return placementSpecialtyDto.orElse(null);
  }

  /**
   * Create a non-null singleton list from the given DTO.
   *
   * @param dto The DTO to check for null.
   * @return An empty list if DTO is null, else a singleton list.
   */
  private List<Object> createNonNullList(Object dto) {
    if (dto != null) {
      return singletonList(dto);
    } else {
      return emptyList();
    }
  }
}
