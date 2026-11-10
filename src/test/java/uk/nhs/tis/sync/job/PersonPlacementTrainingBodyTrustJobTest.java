package uk.nhs.tis.sync.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.tcs.api.enumeration.PersonTrustStatus;
import com.transformuk.hee.tis.tcs.service.model.Person;
import com.transformuk.hee.tis.tcs.service.model.PersonTrust;
import com.transformuk.hee.tis.tcs.service.repository.PersonRepository;
import com.transformuk.hee.tis.tcs.service.repository.PersonTrustRepository;
import com.transformuk.hee.tis.tcs.service.service.helper.SqlQuerySupplier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.nhs.tis.sync.model.EntityData;

@ExtendWith(MockitoExtension.class)
class PersonPlacementTrainingBodyTrustJobTest {

  private static final int PAGE_SIZE = 5000;
  private static final String MOCKED_QUERY_STRING = "SELECT * FROM placement";
  private static final Long LAST_ID = 0L;
  private static final Long LAST_TRUST_ID = 0L;

  @Mock
  private EntityManagerFactory entityManagerFactory;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private SqlQuerySupplier sqlQuerySupplier;
  @Mock
  private PersonTrustRepository personTrustRepository;
  @Mock
  private PersonRepository personRepository;
  @Mock
  private EntityManager entityManager;
  @Mock
  private Query query;

  @InjectMocks
  private PersonPlacementTrainingBodyTrustJob job;

  @Test
  void collectDataShouldMapStatusFromThirdColumn() {
    when(sqlQuerySupplier.getQuery(SqlQuerySupplier.PERSON_PLACEMENT_TRAININGBODY))
        .thenReturn(MOCKED_QUERY_STRING);
    when(entityManager.createNativeQuery(MOCKED_QUERY_STRING)).thenReturn(query);
    when(query.setParameter("lastId", LAST_ID)).thenReturn(query);
    when(query.setParameter("lastTrainingBodyId", LAST_TRUST_ID)).thenReturn(query);
    when(query.setParameter("pageSize", PAGE_SIZE)).thenReturn(query);
    when(query.getResultList()).thenReturn(List.of(
        new Object[]{BigInteger.valueOf(11L), BigInteger.valueOf(22L), "ACTIVE"},
        new Object[]{BigInteger.valueOf(33L), BigInteger.valueOf(44L), "INACTIVE"}
    ));

    Map<String, Long> ids = new HashMap<>();
    ids.put("lastEntityId", LAST_ID);
    ids.put("lastTrustId", LAST_TRUST_ID);

    List<EntityData> result = job.collectData(ids, null, entityManager);

    assertEquals(2, result.size());
    assertEquals(11L, result.get(0).getEntityId());
    assertEquals(22L, result.get(0).getOtherId());
    assertEquals("ACTIVE", result.get(0).getStatus());
    assertEquals(33L, result.get(1).getEntityId());
    assertEquals(44L, result.get(1).getOtherId());
    assertEquals("INACTIVE", result.get(1).getStatus());
  }

  @Test
  void convertDataShouldSetStatus() {
    Person person = new Person();
    person.setId(11L);

    when(personRepository.findAllById(Set.of(11L))).thenReturn(List.of(person));

    Set<PersonTrust> entitiesToSave = new HashSet<>();
    List<EntityData> data = List.of(new EntityData().entityId(11L).otherId(22L).status("INACTIVE"));

    int skipped = job.convertData(entitiesToSave, data, entityManager);

    assertEquals(0, skipped);
    assertEquals(1, entitiesToSave.size());
    PersonTrust savedPersonTrust = entitiesToSave.iterator().next();
    assertEquals(11L, savedPersonTrust.getPerson().getId());
    assertEquals(22L, savedPersonTrust.getTrustId());
    assertEquals(PersonTrustStatus.INACTIVE, savedPersonTrust.getStatus());
  }

  @Test
  void convertDataShouldReturnZeroForEmptyData() {

    Set<PersonTrust> entitiesToSave = new HashSet<>();
    int skipped = job.convertData(entitiesToSave, List.of(), entityManager);

    assertEquals(0, skipped);
    assertEquals(0, entitiesToSave.size());
  }

  @Test
  void convertDataShouldSkipInvalidEntity() {

    Set<PersonTrust> entitiesToSave = new HashSet<>();
    List<EntityData> data = List.of(new EntityData().entityId(null).otherId(22L).status("ACTIVE"));

    int skipped = job.convertData(entitiesToSave, data, entityManager);

    assertEquals(1, skipped);
    assertEquals(0, entitiesToSave.size());
  }

  @Test
  void convertDataShouldSkipMissingPerson() {

    when(personRepository.findAllById(Set.of(11L))).thenReturn(List.of());

    Set<PersonTrust> entitiesToSave = new HashSet<>();
    List<EntityData> data = List.of(new EntityData().entityId(11L).otherId(22L).status("ACTIVE"));

    int skipped = job.convertData(entitiesToSave, data, entityManager);

    assertEquals(1, skipped);
    assertEquals(0, entitiesToSave.size());
  }

  @Test
  void collectDataShouldHandleNullStatusValue() {
    when(sqlQuerySupplier.getQuery(SqlQuerySupplier.PERSON_PLACEMENT_TRAININGBODY))
        .thenReturn(MOCKED_QUERY_STRING);
    when(entityManager.createNativeQuery(MOCKED_QUERY_STRING)).thenReturn(query);
    when(query.setParameter("lastId", LAST_ID)).thenReturn(query);
    when(query.setParameter("lastTrainingBodyId", LAST_TRUST_ID)).thenReturn(query);
    when(query.setParameter("pageSize", PAGE_SIZE)).thenReturn(query);
    List<Object[]> rows = new ArrayList<>();
    rows.add(new Object[]{BigInteger.valueOf(11L), BigInteger.valueOf(22L), null});
    when(query.getResultList()).thenReturn(rows);

    List<EntityData> result = job.collectData(Map.of("lastEntityId", LAST_ID, "lastTrustId",
        LAST_TRUST_ID), null, entityManager);

    assertEquals(1, result.size());
    assertNull(result.get(0).getStatus());
  }
}
