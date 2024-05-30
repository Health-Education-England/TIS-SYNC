package uk.nhs.tis.sync.job;

import com.transformuk.hee.tis.tcs.service.model.Person;
import com.transformuk.hee.tis.tcs.service.model.PersonTrust;
import com.transformuk.hee.tis.tcs.service.repository.PersonRepository;
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

@Component
@ManagedResource(objectName = "sync.mbean:name=PersonPlacementTrainingBodyTrustJob",
    description = "Service that clears the PersonTrust table and links Person with Placement TrainingBody (Trusts)")
@Slf4j
public class PersonPlacementTrainingBodyTrustJob extends TrustAdminSyncJobTemplate<PersonTrust> {

  private static final int FIFTEEN_MIN = 15 * 60 * 1000;

  private PersonRepository personRepository;

  private SqlQuerySupplier sqlQuerySupplier;

  @Autowired
  public PersonPlacementTrainingBodyTrustJob(EntityManagerFactory entityManagerFactory,
      @Autowired(required = false) ApplicationEventPublisher applicationEventPublisher,
      PersonRepository personRepository, SqlQuerySupplier sqlQuerySupplier) {
    super(entityManagerFactory, applicationEventPublisher);
    this.personRepository = personRepository;
    this.sqlQuerySupplier = sqlQuerySupplier;
  }

  @Scheduled(cron = "${application.cron.personPlacementTrainingBodyTrustJob}")
  @SchedulerLock(name = "personTrustTrainingBodyScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(
      description = "Run sync of the PersonTrust table with Person to Placement TrainingBody")
  public void PersonPlacementTrainingBodyFullSync() {
    runSyncJob(null);
  }

  @Override
  protected void deleteData() {
    // This job runs after the PostEmployingBodyEmployingBodyTrustJob and therefore shouldn't
    // truncate the table
  }

  @Override
  protected List<EntityData> collectData(Map<String, Long> ids, String queryString,
                                         EntityManager entityManager) {
    long lastId = ids.get(LAST_ENTITY_ID);
    long lastTrainingBodyId = ids.get(LAST_SITE_ID);
    log.info("Querying with lastPersonId: [{}] and lastTrainingBodyId: [{}]", lastId,
        lastTrainingBodyId);
    String personPlacementQuery =
        sqlQuerySupplier.getQuery(SqlQuerySupplier.PERSON_PLACEMENT_TRAININGBODY);

    Query query = entityManager.createNativeQuery(personPlacementQuery)
        .setParameter("lastId", lastId).setParameter("lastTrainingBodyId", lastTrainingBodyId)
        .setParameter("pageSize", getPageSize());

    List<Object[]> resultList = query.getResultList();
    List<EntityData> result = resultList.stream().filter(Objects::nonNull)
        .map(objArr -> new EntityData().entityId(((BigInteger) objArr[0]).longValue())
            .otherId(((BigInteger) objArr[1]).longValue()))
        .collect(Collectors.toList());

    return result;
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


