package uk.nhs.tis.sync.service;

import com.transformuk.hee.tis.profile.client.service.impl.ProfileServiceImpl;
import com.transformuk.hee.tis.reference.api.dto.DBCDTO;
import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.api.dto.PersonDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementDetailsDTO;
import com.transformuk.hee.tis.tcs.api.dto.PlacementSpecialtyDTO;
import com.transformuk.hee.tis.tcs.api.dto.ProgrammeMembershipDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSpecialtyType;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.nhs.tis.sync.dto.CurriculumMembershipWrapperDto;

@Slf4j
@Service
public class DataRequestService {

  private static final String TABLE_CURRICULUM = "Curriculum";
  private static final String TABLE_CURRICULUM_MEMBERSHIP = "CurriculumMembership";
  private static final String TABLE_DBC = "DBC";
  private static final String TABLE_GRADE = "Grade";
  private static final String TABLE_HEE_USER = "HeeUser";
  private static final String TABLE_LOCAL_OFFICE = "LocalOffice";
  private static final String TABLE_PERSON = "Person";
  private static final String TABLE_PLACEMENT = "Placement";
  private static final String TABLE_PLACEMENT_SPECIALTY = "PlacementSpecialty";
  private static final String TABLE_POST = "Post";
  private static final String TABLE_PROGRAMME = "Programme";
  private static final String TABLE_PROGRAMME_MEMBERSHIP = "ProgrammeMembership";
  private static final String TABLE_SPECIALTY = "Specialty";
  private static final String TABLE_SITE = "Site";
  private static final String TABLE_TRUST = "Trust";

  private final TcsServiceImpl tcsServiceImpl;

  private final ReferenceServiceImpl referenceServiceImpl;

  private final ProfileServiceImpl profileServiceImpl;

  DataRequestService(TcsServiceImpl tcsServiceImpl, ReferenceServiceImpl referenceServiceImpl,
                     ProfileServiceImpl profileServiceImpl) {
    this.tcsServiceImpl = tcsServiceImpl;
    this.referenceServiceImpl = referenceServiceImpl;
    this.profileServiceImpl = profileServiceImpl;
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

      if (table.equals(TABLE_PROGRAMME_MEMBERSHIP) && message.containsKey("uuid")) {
        UUID uuid = UUID.fromString(message.get("uuid"));
        return createNonNullList(tcsServiceImpl.getProgrammeMembershipByUuid(uuid));
      }

      if (table.equals(TABLE_CURRICULUM_MEMBERSHIP) && message.containsKey(
          "programmeMembershipUuid")) {
        UUID uuid = UUID.fromString(message.get("programmeMembershipUuid"));
        ProgrammeMembershipDTO programmeMembership = tcsServiceImpl.getProgrammeMembershipByUuid(
            uuid);

        return programmeMembership.getCurriculumMemberships().stream()
            .map(cm -> new CurriculumMembershipWrapperDto(uuid, cm))
            .collect(Collectors.toList());
      }

      if (table.equals(TABLE_HEE_USER) && message.containsKey("name")) {
        String name = message.get("name");
        return createNonNullList(profileServiceImpl.getSingleAdminUser(name));
      }

      if (table.equals(TABLE_DBC)) {
        ResponseEntity<DBCDTO> responseEntity;
        if (message.containsKey("dbc")) {
          String dbc = message.get("dbc");
          responseEntity = referenceServiceImpl.getDBCByCode(dbc);
          return createNonNullList(responseEntity.getBody());
        }
        if (message.containsKey("abbr")) {
          String abbr = message.get("abbr");
          responseEntity = referenceServiceImpl.getDBCByAbbr(abbr);
          return createNonNullList(responseEntity.getBody());
        }
      }

      if (table.equals(TABLE_LOCAL_OFFICE) && message.containsKey("abbreviation")) {
        String abbreviation = message.get("abbreviation");
        return createNonNullList(
            referenceServiceImpl.findLocalOfficesByAbbrev(abbreviation).get(0));
      }

      if (message.containsKey("id")) {
        long id = Long.parseLong(message.get("id"));

        switch (table) {
          case TABLE_PERSON:
            return retrieveFullSyncData(id);
          case TABLE_CURRICULUM:
            return createNonNullList(tcsServiceImpl.getCurriculumById(id));
          case TABLE_PLACEMENT:
            return createNonNullList(tcsServiceImpl.getPlacementById(id));
          case TABLE_POST:
            return createNonNullList(tcsServiceImpl.getPostById(id));
          case TABLE_PROGRAMME:
            return createNonNullList(
                tcsServiceImpl.findProgrammesIn(Collections.singletonList(String.valueOf(id)))
                    .get(0));
          case TABLE_SITE:
            return createNonNullList(
                referenceServiceImpl.findSitesIdIn(Collections.singleton(id)).get(0));
          case TABLE_SPECIALTY:
            return createNonNullList(tcsServiceImpl.getSpecialtyById(id));
          case TABLE_TRUST:
            return createNonNullList(referenceServiceImpl.findTrustById(id));
          case TABLE_GRADE:
            return createNonNullList(
                referenceServiceImpl.findGradesIdIn(Collections.singleton(id)).get(0));
          default:
            break;
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    return Collections.emptyList();
  }

  private PlacementSpecialtyDTO retrievePlacementSpecialty(long placementId) {
    PlacementDetailsDTO placement = tcsServiceImpl.getPlacementById(placementId);
    Optional<PlacementSpecialtyDTO> placementSpecialtyDto = placement.getSpecialties().stream()
        .filter(ps -> ps.getPlacementSpecialtyType() == PostSpecialtyType.PRIMARY)
        .findFirst();
    return placementSpecialtyDto.orElse(null);
  }

  /**
   * Retrieve all data needed for a full sync of a single person. PersonOwner is excluded due to no
   * existing interaction with it via TCS, the overnight rebuild job will populate it when it runs.
   *
   * @param personId The ID to use to find related data.
   * @return The list of found DTOs.
   */
  private List<Object> retrieveFullSyncData(long personId) {
    List<Object> fullData = new ArrayList<>();

    PersonDTO person = tcsServiceImpl.getPerson(Long.toString(personId));
    fullData.add(person);
    fullData.add(person.getContactDetails());
    fullData.add(person.getGdcDetails());
    fullData.add(person.getGmcDetails());
    fullData.add(person.getPersonalDetails());
    fullData.addAll(tcsServiceImpl.getPlacementForTrainee(personId));
    fullData.addAll(person.getProgrammeMemberships());
    fullData.addAll(person.getQualifications());

    return fullData;
  }

  /**
   * Create a non-null singleton list from the given DTO.
   *
   * @param dto The DTO to check for null.
   * @return An empty list if DTO is null, else a singleton list.
   */
  private List<Object> createNonNullList(Object dto) {
    if (dto != null) {
      return Collections.singletonList(dto);
    } else {
      return Collections.emptyList();
    }
  }
}
