package uk.nhs.tis.sync.job;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.transformuk.hee.tis.tcs.service.model.Person;
import net.javacrumbs.shedlock.core.SchedulerLock;
import uk.nhs.tis.sync.event.JobExecutionEvent;

@Component
@ManagedResource(objectName = "sync.mbean:name=PersonRecordStatusJob",
    description = "Job set a Person's (Training Record) Status if their programme membership(s) started/ended")
public class PersonRecordStatusJob {

  protected static final int DEFAULT_PAGE_SIZE = 5000;
  private static final Logger LOG = LoggerFactory.getLogger(PersonRecordStatusJob.class);
  private static final int FIFTEEN_MIN = 15 * 60 * 1000;
  private static final String BASE_QUERY =
      "SELECT DISTINCT personId FROM ProgrammeMembership" + " WHERE personId > :lastPersonId"
          + " AND (programmeEndDate = ':endDate' OR programmeStartDate = ':startDate')"
          + " ORDER BY personId LIMIT :pageSize";
  private static final String FULL_SYNC_DATE_STR = "ANY";
  private static final String NO_OVERWRITE_TO_AWS_DATE_STR = "AWS";

  @Autowired
  private EntityManagerFactory entityManagerFactory;
  @Value("${application.jobs.personRecordStatusJob.dateOfChangeOverride:}")
  private String dateOfChangeOverride;
  private Stopwatch mainStopWatch;

  @Scheduled(cron = "${application.cron.personRecordStatusJob}")
  @SchedulerLock(name = "personRecordStatusScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(
      description = "Run sync of the PersonTrust table with Person to Placement TrainingBody")
  public void personRecordStatusJob() {
    runSyncJob(null);
  }

  /**
   * trigger the personRecordStatusJob with the specified date.
   *
   * @param dateStr can be "ANY", "AWS", empty or a date in format yyyy-MM-dd
   */
  public void personRecordStatusJob(String dateStr) {
    if (StringUtils.isEmpty(dateStr)) {
      runSyncJob(null);
    } else {
      try {
        validateDateParamFormat(dateStr);
      } catch (DateTimeParseException e) {
        String errorMsg = String.format("The date is not correct: %s", dateStr);
        LOG.error(errorMsg);
        throw new IllegalArgumentException(errorMsg);
      }
      runSyncJob(dateStr);
    }
  }

  private void validateDateParamFormat(String dateStr) throws DateTimeParseException {
    if (!StringUtils.isEmpty(dateStr) && !StringUtils.equalsIgnoreCase(dateStr, FULL_SYNC_DATE_STR)
        && !StringUtils.equalsIgnoreCase(dateStr, NO_OVERWRITE_TO_AWS_DATE_STR)) {
      // if the date format is incorrect, throw a DateTimeParseException
      LocalDate parsedDate = LocalDate.parse(dateStr);
    }
  }

  protected String getJobName() {
    return this.getClass().getSimpleName();
  }

  protected int getPageSize() {
    return DEFAULT_PAGE_SIZE;
  }

  protected EntityManagerFactory getEntityManagerFactory() {
    return entityManagerFactory;
  }

  protected List<Long> collectData(int pageSize, long lastPersonId, String queryString,
      EntityManager entityManager) {
    Query query =
        entityManager.createNativeQuery(queryString).setParameter("lastPersonId", lastPersonId);

    List<BigInteger> resultList = query.getResultList();
    List<Long> data = resultList.stream().filter(Objects::nonNull)
        .map(result -> Long.parseLong(result.toString()))
        .collect(Collectors.toList());
    return data;
  }

  protected int convertData(Set<Person> entitiesToSave, List<Long> entityData,
      EntityManager entityManager) {
    int entities = entityData.size();
    entityData.stream().map((id) -> {return entityManager.find(Person.class, id);})
        .filter(Objects::nonNull)
        .filter(p -> p.getStatus() != p.programmeMembershipsStatus())
        .peek(p -> p.setStatus(p.programmeMembershipsStatus()))
        .forEach(entitiesToSave::add);
    return entities - entitiesToSave.size();
  }

  @Autowired(required = false)
  private ApplicationEventPublisher applicationEventPublisher;

  @ManagedOperation(description = "Is the sync job currently running")
  public boolean isCurrentlyRunning() {
    return mainStopWatch != null;
  }

  @ManagedOperation(description = "The current elapsed time of the current sync job")
  public String elapsedTime() {
    return mainStopWatch != null ? mainStopWatch.toString() : "0s";
  }

  protected void runSyncJob(String dateStr) {
    if (mainStopWatch != null) {
      LOG.info("Sync job [{}] already running, exiting this execution", getJobName());
      return;
    }
    CompletableFuture.runAsync(() -> run(dateStr));
  }

  protected void run(String dateStr) {
    // Configure run
    LocalDate dateOfChange;
    try {
      dateOfChange = magicallyGetDateOfChanges(dateStr);
    } catch (Exception e) {
      LOG.error(e.getMessage());
      return;
    }
    String queryString;
    if (dateOfChange != null) {
      String startDate = dateOfChange.format(DateTimeFormatter.ISO_LOCAL_DATE);
      String endDate = dateOfChange.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
      queryString = BASE_QUERY.replace(":endDate", endDate).replace(":startDate", startDate)
          .replace(":pageSize", "" + getPageSize());
    } else {
      queryString = BASE_QUERY.replace(":pageSize", "" + getPageSize())
          .replace(" AND (programmeEndDate = ':endDate' OR programmeStartDate = ':startDate')", "");
    }
    int skipped = 0, totalRecords = 0;
    long lastEntityId = 0;
    boolean hasMoreResults = true;
    Set<Person> dataToSave = Sets.newHashSet();
    LOG.debug("Job will run with query:[{}]", queryString);

    if (applicationEventPublisher != null) {
      applicationEventPublisher
          .publishEvent(new JobExecutionEvent(this, "Sync [" + getJobName() + "] started."));
    }
    LOG.info("Sync [{}] started", getJobName());
    mainStopWatch = Stopwatch.createStarted();
    Stopwatch stopwatch = Stopwatch.createStarted();

    while (hasMoreResults) {
      EntityManager entityManager = getEntityManagerFactory().createEntityManager();
      EntityTransaction transaction = null;
      try {
        transaction = entityManager.getTransaction();
        transaction.begin();

        List<Long> collectedData =
            collectData(getPageSize(), lastEntityId, queryString, entityManager);
        hasMoreResults = collectedData.size() > 0;
        LOG.info("Time taken to read chunk : [{}]", stopwatch.toString());

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
        LOG.debug("Collected {} records and attempted to process {}.", collectedData.size(), dataToSave.size());
        dataToSave.clear();

        transaction.commit();
        entityManager.close();
      } catch (Exception e) {
        LOG.error("An error occurred while running the scheduled job", e);
        mainStopWatch = null;
        if (transaction != null && transaction.isActive()) {
          transaction.rollback();
        }
        if (applicationEventPublisher != null) {
          applicationEventPublisher.publishEvent(
              new JobExecutionEvent(this, getFailureMessage(Optional.ofNullable(getJobName()), e)));
        }
        throw e;
      } finally {
        if (entityManager != null && entityManager.isOpen()) {
          entityManager.close();
        }
      }
      LOG.info("Time taken to save chunk : [{}]", stopwatch.toString());
      stopwatch.reset().start();
    }
    LOG.info("Sync job [{}] finished. Total time taken {} for processing [{}] records",
        getJobName(), mainStopWatch.stop().toString(), totalRecords);
    LOG.info("Skipped records {}", skipped);
    mainStopWatch = null;
    if (applicationEventPublisher != null) {
      applicationEventPublisher.publishEvent(
          new JobExecutionEvent(this, getSuccessMessage(Optional.ofNullable(getJobName()))));
    }
  }

  private LocalDate magicallyGetDateOfChanges(String dateStr) {
    if (StringUtils.equalsIgnoreCase(dateStr, NO_OVERWRITE_TO_AWS_DATE_STR)) {
      return getDateOfChanges(dateOfChangeOverride);
    } else {
      return getDateOfChanges(dateStr);
    }
  }

  private LocalDate getDateOfChanges(String dateInUse) {
    if (StringUtils.isEmpty(dateInUse)) {
      return LocalDate.now();
    } else if (FULL_SYNC_DATE_STR.equalsIgnoreCase(dateInUse)) {
      return null;
    } else {
      return LocalDate.parse(dateInUse);
    }
  }

  private void setDateOfChange(String dateOfChangeOverride) {
    this.dateOfChangeOverride = dateOfChangeOverride;
  }

  protected String getSuccessMessage(Optional<String> jobName) {
    return "Sync [" + jobName.orElse(getJobName()) + "] completed successfully.";
  }

  protected String getFailureMessage(Optional<String> jobName, Exception e) {
    return "<!channel> Sync [" + jobName.orElse(getJobName()) + "] failed with exception ["
        + e.getMessage() + "].";
  }
}
