package uk.nhs.tis.sync.job;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jmx.export.annotation.ManagedOperation;
import uk.nhs.tis.sync.event.JobExecutionEvent;
import uk.nhs.tis.sync.model.EntityData;

public abstract class TrustAdminSyncJobTemplate<E> implements RunnableJob {

  private static final Logger LOG = LoggerFactory.getLogger(TrustAdminSyncJobTemplate.class);

  @Autowired(required = false)
  private ApplicationEventPublisher applicationEventPublisher;

  private Stopwatch mainStopWatch;

  protected static final int DEFAULT_PAGE_SIZE = 5000;

  @ManagedOperation(description = "Is the Post Trust sync just currently running")
  public boolean isCurrentlyRunning() {
    return mainStopWatch != null;
  }

  @ManagedOperation(description = "The current elapsed time of the current sync job")
  public String elapsedTime() {
    return mainStopWatch != null ? mainStopWatch.toString() : "0s";
  }

  protected void runSyncJob() {
    if (mainStopWatch != null) {
      LOG.info("Sync job [{}] already running, exiting this execution", getJobName());
      return;
    }
    CompletableFuture.runAsync(this::run)
        .exceptionally(t -> {
          publishJobexecutionEvent(
              new JobExecutionEvent(this, getFailureMessage(Optional.ofNullable(getJobName()), t)));
          LOG.error("Job run ended due an Exception", t);
          return null;
        });
  }

  protected abstract String getJobName();

  protected abstract int getPageSize();

  protected abstract EntityManagerFactory getEntityManagerFactory();

  protected abstract void deleteData();

  protected abstract List<EntityData> collectData(int pageSize, long lastId, long lastSiteId,
      EntityManager entityManager);

  protected abstract int convertData(int skipped, Set<E> entitiesToSave,
      List<EntityData> entityData, EntityManager entityManager);

  public void run(String params) {
    runSyncJob();
  }

  protected void run() {
    publishJobexecutionEvent(new JobExecutionEvent(this, "Sync [" + getJobName() + "] started."));
    LOG.info("Sync [{}] started", getJobName());
    mainStopWatch = Stopwatch.createStarted();
    Stopwatch stopwatch = Stopwatch.createStarted();

    int skipped = 0;
    int totalRecords = 0;
    long lastEntityId = 0;
    long lastSiteId = 0;
    boolean hasMoreResults = true;

    deleteData();
    stopwatch.reset().start();

    Set<E> dataToSave = Sets.newHashSet();

    EntityManager entityManager = null;

    EntityTransaction transaction = null;
    try {
      while (hasMoreResults) {
        entityManager = getEntityManagerFactory().createEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();

        List<EntityData> collectedData =
            collectData(getPageSize(), lastEntityId, lastSiteId, entityManager);
        hasMoreResults = !collectedData.isEmpty();
        LOG.info("Time taken to read chunk : [{}]", stopwatch);

        if (CollectionUtils.isNotEmpty(collectedData)) {
          lastEntityId = collectedData.get(collectedData.size() - 1).getEntityId();
          lastSiteId = collectedData.get(collectedData.size() - 1).getOtherId();
          totalRecords += collectedData.size();
          skipped = convertData(skipped, dataToSave, collectedData, entityManager);
        }
        stopwatch.reset().start();
        dataToSave.forEach(entityManager::persist);
        entityManager.flush();
        dataToSave.clear();

        transaction.commit();
        LOG.info("Time taken to save chunk : [{}]", stopwatch);
        stopwatch.reset().start();
      }
      LOG.info("Sync job [{}] finished. Total time taken {} for processing [{}] records",
          getJobName(), mainStopWatch.stop(), totalRecords);
      LOG.info("Skipped records {}", skipped);
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

  private void publishJobexecutionEvent(JobExecutionEvent event) {
    if (applicationEventPublisher != null) {
      applicationEventPublisher.publishEvent(event);
    }
  }
}
