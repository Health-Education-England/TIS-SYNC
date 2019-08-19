package uk.nhs.tis.sync.job;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import uk.nhs.tis.sync.event.JobExecutionEvent;
import uk.nhs.tis.sync.model.EntityData;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jmx.export.annotation.ManagedOperation;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public abstract class TrustAdminSyncJobTemplate<ENTITY> {

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
    CompletableFuture.runAsync(this::run);
  }

  protected abstract String getJobName();

  protected abstract int getPageSize();

  protected abstract EntityManagerFactory getEntityManagerFactory();

  protected abstract void deleteData();

  protected abstract List<EntityData> collectData(int pageSize, long lastId, long lastSiteId,
      EntityManager entityManager);

  protected abstract int convertData(int skipped, Set<ENTITY> entitiesToSave,
      List<EntityData> entityData, EntityManager entityManager);

  protected void run() {

    if (applicationEventPublisher != null) {
      applicationEventPublisher
          .publishEvent(new JobExecutionEvent(this, "Sync [" + getJobName() + "] started."));
    }
    LOG.info("Sync [{}] started", getJobName());
    mainStopWatch = Stopwatch.createStarted();
    Stopwatch stopwatch = Stopwatch.createStarted();

    int skipped = 0, totalRecords = 0;
    long lastEntityId = 0;
    long lastSiteId = 0;
    boolean hasMoreResults = true;

    deleteData();
    stopwatch.reset().start();

    Set<ENTITY> dataToSave = Sets.newHashSet();

    while (hasMoreResults) {
      EntityManager entityManager = getEntityManagerFactory().createEntityManager();
      EntityTransaction transaction = null;
      try {
        transaction = entityManager.getTransaction();
        transaction.begin();

        List<EntityData> collectedData =
            collectData(getPageSize(), lastEntityId, lastSiteId, entityManager);
        hasMoreResults = collectedData.size() > 0;
        LOG.info("Time taken to read chunk : [{}]", stopwatch.toString());

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
        entityManager.close();
      } catch (Exception e) {
        LOG.error("An error occurred while running the scheduled job", e);
        mainStopWatch = null;
        if (transaction != null && transaction.isActive()) {
          transaction.rollback();
        }
        if (applicationEventPublisher != null) {
          applicationEventPublisher
              .publishEvent(new JobExecutionEvent(this, getFailureMessage(getJobName(), e)));
        }
        throw e;
      } finally {
        if (entityManager != null && entityManager.isOpen()) {
          entityManager.close();
        }
      }
      LOG.info("Time taken to save chunk : [{}]", stopwatch.toString());
    }
    stopwatch.reset().start();
    LOG.info("Sync job [{}] finished. Total time taken {} for processing [{}] records",
        getJobName(), mainStopWatch.stop().toString(), totalRecords);
    LOG.info("Skipped records {}", skipped);
    mainStopWatch = null;
    if (applicationEventPublisher != null) {
      applicationEventPublisher
          .publishEvent(new JobExecutionEvent(this, getSuccessMessage(getJobName())));
    }
  }

  protected String getSuccessMessage(String jobName) {
    return "Sync [" + getJobName() + "] completed successfully.";
  }

  protected String getFailureMessage(String jobName, Exception e) {
    return "@channel Sync [" + getJobName() + "] failed with exception [" + e.getMessage() + "].";
  }

}
