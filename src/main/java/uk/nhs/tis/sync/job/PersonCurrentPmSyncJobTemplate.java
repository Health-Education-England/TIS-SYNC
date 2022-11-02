package uk.nhs.tis.sync.job;

import com.google.common.base.Stopwatch;
import com.transformuk.hee.tis.tcs.service.model.Person;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jmx.export.annotation.ManagedOperation;
import uk.nhs.tis.sync.event.JobExecutionEvent;

/**
 * abstract template for Jobs which sync data when current programmeMembership changes.
 */
public abstract class PersonCurrentPmSyncJobTemplate implements RunnableJob {

  protected static final int DEFAULT_PAGE_SIZE = 5000;
  protected static final int FIFTEEN_MIN = 15 * 60 * 1000;
  protected static final String FULL_SYNC_DATE_STR = "ANY";
  protected static final String NO_DATE_OVERRIDE = "NONE";
  private static final Logger LOG = LoggerFactory.getLogger(PersonCurrentPmSyncJobTemplate.class);
  private static final String BASE_QUERY =
      "SELECT DISTINCT personId FROM ProgrammeMembership" + " WHERE personId > :lastPersonId"
          + " AND (programmeEndDate = ':endDate' OR programmeStartDate = ':startDate')"
          + " ORDER BY personId LIMIT :pageSize";
  protected Stopwatch mainStopWatch;
  @Autowired(required = false)
  protected ApplicationEventPublisher applicationEventPublisher;

  protected String getJobName() {
    return this.getClass().getSimpleName();
  }

  protected int getPageSize() {
    return DEFAULT_PAGE_SIZE;
  }

  protected abstract EntityManagerFactory getEntityManagerFactory();

  protected abstract String getDateOfChangeOverride();

  protected List<Long> collectData(long lastPersonId, String queryString,
      EntityManager entityManager) {
    Query query =
        entityManager.createNativeQuery(queryString).setParameter("lastPersonId", lastPersonId);

    List<BigInteger> resultList = query.getResultList();
    return resultList.stream().filter(Objects::nonNull)
        .map(result -> Long.parseLong(result.toString()))
        .collect(Collectors.toList());
  }

  protected int convertData(Set<Person> entitiesToSave, List<Long> entityData,
      EntityManager entityManager) {
    int entities = entityData.size();
    entityData.stream().map(id -> entityManager.find(Person.class, id))
        .filter(Objects::nonNull)
        .filter(p -> p.getStatus() != p.programmeMembershipsStatus())
        .forEach(p -> {
          p.setStatus(p.programmeMembershipsStatus());
          entitiesToSave.add(p);
        });
    return entities - entitiesToSave.size();
  }

  @ManagedOperation(description = "Is the sync job currently running")
  public boolean isCurrentlyRunning() {
    return mainStopWatch != null;
  }

  @ManagedOperation(description = "The current elapsed time of the current sync job")
  public String elapsedTime() {
    return mainStopWatch != null ? mainStopWatch.toString() : "0s";
  }

  protected void runSyncJob(String dateOption) {
    if (mainStopWatch != null) {
      LOG.info("Sync job [{}] already running, exiting this execution", getJobName());
      return;
    }
    CompletableFuture.runAsync(() -> runRecordStatusSync(dateOption))
        .exceptionally(t -> {
          publishJobexecutionEvent(
              new JobExecutionEvent(this, getFailureMessage(Optional.ofNullable(getJobName()), t)));
          LOG.error("Job run ended due an Exception", t);
          return null;
        });
  }

  protected abstract void doDataSync(String queryString);

  private void runRecordStatusSync(String dateOption) {
    // Configure run
    LocalDate dateOfChange = magicallyGetDateOfChanges(dateOption);
    String queryString = buildQueryForDate(dateOfChange);

    LOG.debug("Job will run with query:[{}]", queryString);

    publishJobexecutionEvent(new JobExecutionEvent(this, "Sync [" + getJobName() + "] started."));
    LOG.info("Sync [{}] started", getJobName());
    mainStopWatch = Stopwatch.createStarted();

    doDataSync(queryString);
  }

  private String buildQueryForDate(LocalDate dateOfChange) {
    if (dateOfChange != null) {
      String startDate = dateOfChange.format(DateTimeFormatter.ISO_LOCAL_DATE);
      String endDate = dateOfChange.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
      return BASE_QUERY.replace(":endDate", endDate).replace(":startDate", startDate)
          .replace(":pageSize", "" + getPageSize());
    } else {
      return BASE_QUERY.replace(":pageSize", "" + getPageSize())
          .replace(" AND (programmeEndDate = ':endDate' OR programmeStartDate = ':startDate')", "");
    }
  }

  private LocalDate magicallyGetDateOfChanges(String dateToUse) {
    if (StringUtils.equalsIgnoreCase(dateToUse, NO_DATE_OVERRIDE)) {
      dateToUse = getDateOfChangeOverride();
    }
    if (StringUtils.isEmpty(dateToUse)) {
      return LocalDate.now();
    }
    if (FULL_SYNC_DATE_STR.equalsIgnoreCase(dateToUse)) {
      return null;
    }
    return LocalDate.parse(dateToUse);
  }

  protected String getSuccessMessage(Optional<String> jobName) {
    return "Sync [" + jobName.orElse(getJobName()) + "] completed successfully.";
  }

  protected String getFailureMessage(Optional<String> jobName, Throwable e) {
    return "<!channel> Sync [" + jobName.orElse(getJobName()) + "] failed with exception ["
        + e.getMessage() + "].";
  }

  protected void publishJobexecutionEvent(JobExecutionEvent event) {
    if (applicationEventPublisher != null) {
      applicationEventPublisher.publishEvent(event);
    }
  }
}
