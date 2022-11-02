package uk.nhs.tis.sync.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.transformuk.hee.tis.tcs.service.model.Person;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.event.JobExecutionEvent;
import uk.nhs.tis.sync.message.publisher.RabbitMqTcsPmUpdatePublisher;

@Component
@ManagedResource(objectName = "sync.mbean:name=PersonRecordStatusJob",
    description = "Job set a Person's (Training Record) Status if their programme membership(s) started/ended")
public class PersonRecordStatusJob extends PersonCurrentPmSyncJobTemplate {

  private static final Logger LOG = LoggerFactory.getLogger(PersonRecordStatusJob.class);

  private final EntityManagerFactory entityManagerFactory;
  private final ObjectMapper objectMapper;

  @Value("${application.jobs.personRecordStatusJob.dateOfChangeOverride}")
  private String dateOfChangeOverride;

  public PersonRecordStatusJob(EntityManagerFactory entityManagerFactory,
      ObjectMapper objectMapper,
      RabbitMqTcsPmUpdatePublisher rabbitMqPublisher) {
    this.entityManagerFactory = entityManagerFactory;
    this.objectMapper = objectMapper;
  }

  public void run(String params) {
    personRecordStatusJob(params);
  }

  @Scheduled(cron = "${application.cron.personRecordStatusJob}")
  @SchedulerLock(name = "personRecordStatusScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(
      description = "Run sync of the ProgrammeMembership table to update Person status")
  public void personRecordStatusJob() {
    runSyncJob(null);
  }

  /**
   * Trigger the personRecordStatusJob with the specified date.
   *
   * @param jsonParams The only recognised property is `dateOverride`.  The value can be "ANY",
   *                   "NONE", empty or a date in format yyyy-MM-dd
   */
  public void personRecordStatusJob(String jsonParams) {
    LOG.debug("Received run params [{}]", jsonParams);
    String date = null;
    if (StringUtils.isNotEmpty(jsonParams)) {
      try {
        date = objectMapper.readTree(jsonParams).get("dateOverride").textValue();
        validateDateParamFormat(date);
        LOG.debug("Got validated date [{}]", date);
      } catch (JsonProcessingException e) {
        String errorMsg = "Unable to extract the dateOverride property";
        LOG.error(errorMsg, e);
        throw new IllegalArgumentException(errorMsg);
      } catch (DateTimeParseException e) {
        String errorMsg = String.format("The date is not correct: %s", date);
        LOG.error(errorMsg, e);
        throw new IllegalArgumentException(errorMsg);
      }
    }
    runSyncJob(date);
  }

  private LocalDate validateDateParamFormat(String dateStr) throws DateTimeParseException {
    if (StringUtils.isEmpty(dateStr) || StringUtils.equalsIgnoreCase(dateStr, FULL_SYNC_DATE_STR)
        || StringUtils.equalsIgnoreCase(dateStr, NO_DATE_OVERRIDE)) {
      return null;
    }
    // if the date format is incorrect, throw a DateTimeParseException
    return LocalDate.parse(dateStr);
  }

  protected EntityManagerFactory getEntityManagerFactory() {
    return entityManagerFactory;
  }

  protected String getDateOfChangeOverride() {
    return dateOfChangeOverride;
  }

  protected void doDataSync(String queryString) {
    int skipped = 0;
    int totalRecords = 0;
    long lastEntityId = 0;
    boolean hasMoreResults = true;
    Set<Person> dataToSave = Sets.newHashSet();

    Stopwatch stopwatch = Stopwatch.createStarted();

    EntityManager entityManager = null;
    EntityTransaction transaction = null;
    try {
      while (hasMoreResults) {
        entityManager = getEntityManagerFactory().createEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        List<Long> collectedData =
            collectData(lastEntityId, queryString, entityManager);
        hasMoreResults = !collectedData.isEmpty();
        LOG.info("Time taken to read chunk : [{}]", stopwatch);

        if (CollectionUtils.isNotEmpty(collectedData)) {
          lastEntityId = collectedData.get(collectedData.size() - 1);
          totalRecords += collectedData.size();
          skipped += convertData(dataToSave, collectedData, entityManager);
        }
        stopwatch.reset().start();
        if (CollectionUtils.isNotEmpty(dataToSave)) {
          dataToSave.forEach(entityManager::persist);
          entityManager.flush();
        }
        LOG.debug("Collected {} records and attempted to process {}.", collectedData.size(),
            dataToSave.size());
        dataToSave.clear();

        transaction.commit();
        entityManager.close();
        LOG.info("Time taken to save chunk : [{}]", stopwatch);
        stopwatch.reset().start();
      }
      LOG.info("Sync job [{}] finished. Total time taken {} for processing [{}] records",
          getJobName(), mainStopWatch.stop(), totalRecords);
      LOG.info("Skipped records {}", skipped);
      mainStopWatch = null;
      publishJobexecutionEvent(
          new JobExecutionEvent(this, getSuccessMessage(Optional.ofNullable(getJobName()))));
    } finally {
      mainStopWatch = null;
      if (transaction != null && transaction.isActive()) {
        transaction.rollback();
      }
      if (entityManager != null && entityManager.isOpen()) {
        entityManager.close();
      }
    }
  }
}
