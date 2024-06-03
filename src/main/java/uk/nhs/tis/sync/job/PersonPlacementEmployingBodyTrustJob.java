package uk.nhs.tis.sync.job;

import com.transformuk.hee.tis.tcs.service.model.Person;
import com.transformuk.hee.tis.tcs.service.model.PersonTrust;
import com.transformuk.hee.tis.tcs.service.repository.PersonRepository;
import com.transformuk.hee.tis.tcs.service.repository.PersonTrustRepository;
import com.transformuk.hee.tis.tcs.service.service.helper.SqlQuerySupplier;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.model.EntityData;

/**
 * A sync job for updating placement employing body for a person.
 *
 * This job clears the PersonTrust table and links Person with Placement EmployingBody (Trust).
 *
 */
@Component
@ManagedResource(objectName = "sync.mbean:name=PersonPlacementEmployingBodyJob",
    description = "Service that clears the PersonTrust table and links "
        + "Person with Placement EmployingBody(Trust)")
@SuppressWarnings("unchecked")
@Slf4j
public class PersonPlacementEmployingBodyTrustJob extends TrustAdminSyncJobTemplate<PersonTrust> {

  private final PersonTrustRepository personTrustRepository;

  private final PersonRepository personRepository;

  private final SqlQuerySupplier sqlQuerySupplier;

  /**
   * Constructs a new PersonPlacementEmployingBodyTrustJob with the specified dependencies.
   *
   * @param entityManagerFactory the factory to create EntityManager instances
   * @param applicationEventPublisher the publisher for application events, may be null
   * @param sqlQuerySupplier the supplier for SQL queries
   * @param personTrustRepository the repository for PersonTrust entities
   * @param personRepository the repository for Person entities
   */
  public PersonPlacementEmployingBodyTrustJob(
      EntityManagerFactory entityManagerFactory,
      @Autowired(required = false) ApplicationEventPublisher applicationEventPublisher,
      SqlQuerySupplier sqlQuerySupplier, PersonTrustRepository personTrustRepository,
      PersonRepository personRepository) {
    super(entityManagerFactory, applicationEventPublisher);
    this.sqlQuerySupplier = sqlQuerySupplier;
    this.personTrustRepository = personTrustRepository;
    this.personRepository = personRepository;
  }

  @Scheduled(cron = "${application.cron.personPlacementEmployingBodyTrustJob}")
  @SchedulerLock(name = "personTrustEmployingBodyScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(
      description = "Run sync of the PersonTrust table with Person to Placement EmployingBody")
  public void doPersonPlacementEmployingBodyFullSync() {
    runSyncJob(null);
  }

  @Override
  protected void deleteData() {
    log.info("deleting all data");
    personTrustRepository.deleteAllInBatch();
    log.info("deleted all PersonTrust data");
  }

  @Override
  protected List<EntityData> collectData(Map<String, Long> ids, String queryString,
                                         EntityManager entityManager) {
    long lastId = ids.get(LAST_ENTITY_ID);
    long lastEmployingBodyId = ids.get(LAST_SITE_ID);
    log.info("Querying with lastPersonId: [{}] and lastEmployingBodyId: [{}]", lastId,
        lastEmployingBodyId);
    String personPlacementQuery =
        sqlQuerySupplier.getQuery(SqlQuerySupplier.PERSON_PLACEMENT_EMPLOYINGBODY);

    Query query = entityManager.createNativeQuery(personPlacementQuery)
        .setParameter("lastId", lastId).setParameter("lastEmployingBodyId", lastEmployingBodyId)
        .setParameter("pageSize", getPageSize());
    List<Object[]> resultList = query.getResultList();

    return resultList.stream().filter(Objects::nonNull)
        .map(objArr -> new EntityData().entityId(((BigInteger) objArr[0]).longValue())
            .otherId(((BigInteger) objArr[1]).longValue()))
        .collect(Collectors.toList());
  }

  @Override
  protected int convertData(Set<PersonTrust> entitiesToSave,
                            List<EntityData> entityData, EntityManager entityManager) {

    int skipped = 0;
    if (CollectionUtils.isNotEmpty(entityData)) {

      Set<Long> personIds =
          entityData.stream().map(EntityData::getEntityId).collect(Collectors.toSet());
      List<Person> allPersons = personRepository.findAllById(personIds);
      Map<Long, Person> personIdToPerson =
          allPersons.stream().collect(Collectors.toMap(Person::getId, person -> person));

      for (EntityData ed : entityData) {
        if (ed != null) {
          if (ed.getEntityId() != null || ed.getOtherId() != null) {
            PersonTrust personTrust = new PersonTrust();
            personTrust.setPerson(personIdToPerson.get(ed.getEntityId()));
            personTrust.setTrustId(ed.getOtherId());

            entitiesToSave.add(personTrust);
          } else {
            skipped++;
          }
        }
      }
    }
    return skipped;
  }
}
