package uk.nhs.tis.sync.job;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jmx.export.annotation.ManagedOperation;
import uk.nhs.tis.sync.event.JobExecutionEvent;
import uk.nhs.tis.sync.model.EntityData;

/**
 * An abstract common sync job template.
 *
 * @param <T> the type of the entity loaded into the target
 */
@Slf4j
public abstract class CommonSyncJobTemplate<T> implements RunnableJob {

  private static final int DEFAULT_PAGE_SIZE = 5000;
  protected static final int FIFTEEN_MIN = 15 * 60 * 1000;

  protected Stopwatch mainStopWatch;

  protected final ApplicationEventPublisher applicationEventPublisher;

  private EntityManagerFactory entityManagerFactory;

  protected CommonSyncJobTemplate(EntityManagerFactory entityManagerFactory,
      @Autowired(required = false) ApplicationEventPublisher applicationEventPublisher) {
    this.entityManagerFactory = entityManagerFactory;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  protected String getJobName() {
    return this.getClass().getSimpleName();
  }

  protected int getPageSize() {
    return DEFAULT_PAGE_SIZE;
  }

  protected EntityManagerFactory getEntityManagerFactory() {
    return this.entityManagerFactory;
  }

  protected abstract void deleteData();

  protected abstract int convertData(Set<T> entitiesToSave, List<EntityData> entityData,
      EntityManager entityManager);

  protected abstract void handleData(Set<T> dataToSave, EntityManager entityManager);

  protected abstract List<EntityData> collectData(Map<String, Long> ids, String queryString,
      EntityManager entityManager);

  @ManagedOperation(description = "Is the sync job currently running")
  public boolean isCurrentlyRunning() {
    return mainStopWatch != null;
  }

  @ManagedOperation(description = "The current elapsed time of the current sync job")
  public String elapsedTime() {
    return mainStopWatch != null ? mainStopWatch.toString() : "0s";
  }

  protected void runSyncJob(String option) {
    if (mainStopWatch != null) {
      log.info("Sync job [{}] already running, exiting this execution", getJobName());
      return;
    }
    CompletableFuture.runAsync(() -> doDataSync(option))
        .exceptionally(t -> {
          publishJobexecutionEvent(
              new JobExecutionEvent(this, getFailureMessage(Optional.ofNullable(getJobName()), t)));
          log.error("Job run ended due an Exception", t);
          return null;
        });
  }

  /**
   * Put all ids needed for the query in a map.
   *
   * @return the map containing initialised ids
   */
  protected abstract Map<String, Long> initIds();

  /**
   * Update ids in the map to the last entity id we collected.
   *
   * @param ids           ids to be updated in the iteration of processing
   * @param collectedData collected entities from db
   */
  protected abstract void updateIds(Map<String, Long> ids, List<EntityData> collectedData);

  /**
   * Assemble the query string in the sync job. This method is not necessary if the query is
   * assembled in the `collectedData` method.
   *
   * @param option parameters needed to assemble the query string
   * @return the query string
   */
  protected abstract String assembleQueryString(String option);

  protected void doDataSync(String option) {
    publishJobexecutionEvent(new JobExecutionEvent(this, "Sync [" + getJobName() + "] started."));
    log.info("Sync [{}] started", getJobName());

    mainStopWatch = Stopwatch.createStarted();
    Stopwatch stopwatch = Stopwatch.createStarted();

    int skipped = 0;
    int totalRecords = 0;
    boolean hasMoreResults = true;
    Map<String, Long> ids = initIds();
    String queryString = assembleQueryString(option);

    EntityManager entityManager = null;
    EntityTransaction transaction = null;

    Set<T> dataToSave = Sets.newHashSet();

    deleteData();
    stopwatch.reset().start();

    try {
      while (hasMoreResults) {
        entityManager = getEntityManagerFactory().createEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        List<EntityData> collectedData =
            collectData(ids, queryString, entityManager);
        hasMoreResults = !collectedData.isEmpty();
        log.info("Time taken to read chunk : [{}]", stopwatch);

        if (CollectionUtils.isNotEmpty(collectedData)) {
          updateIds(ids, collectedData);
          totalRecords += collectedData.size();
          skipped += convertData(dataToSave, collectedData, entityManager);
        }
        stopwatch.reset().start();

        handleData(dataToSave, entityManager);

        dataToSave.clear();

        transaction.commit();
        log.info("Time taken to save chunk : [{}]", stopwatch);
        stopwatch.reset().start();
        entityManager.close();
      }
      log.info("Sync job [{}] finished. Total time taken {} for processing [{}] records",
          getJobName(), mainStopWatch.stop(), totalRecords);
      log.info("Skipped records {}", skipped);
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
