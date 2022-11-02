package uk.nhs.tis.sync.job.reval;

import com.google.common.base.Stopwatch;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.event.JobExecutionEvent;
import uk.nhs.tis.sync.job.PersonCurrentPmSyncJobTemplate;
import uk.nhs.tis.sync.message.publisher.RabbitMqTcsPmUpdatePublisher;

@Component
@ManagedResource(objectName = "sync.mbean:name=RevalCurrentPmSyncJob",
    description = "Job trigger exporting current ProgrammeMembership from TCS to Reval when nightly date changes")
public class RevalCurrentPmSyncJob extends PersonCurrentPmSyncJobTemplate {

  private static final Logger LOG = LoggerFactory.getLogger(RevalCurrentPmSyncJob.class);

  private final EntityManagerFactory entityManagerFactory;
  private final RabbitMqTcsPmUpdatePublisher rabbitMqPublisher;

  public RevalCurrentPmSyncJob(EntityManagerFactory entityManagerFactory,
      RabbitMqTcsPmUpdatePublisher rabbitMqPublisher) {
    this.entityManagerFactory = entityManagerFactory;
    this.rabbitMqPublisher = rabbitMqPublisher;
  }

  @Override
  public void run(String params) {
    revalCurrentPmSyncJob();
  }

  @Scheduled(cron = "${application.cron.revalCurrentPmJob}")
  @SchedulerLock(name = "revalCurrentPmScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(
      description = "Get PersonIds from ProgrammeMembership table and sends them to tcs for current PM Sync")
  public void revalCurrentPmSyncJob() {
    runSyncJob(null);
  }

  protected EntityManagerFactory getEntityManagerFactory() {
    return entityManagerFactory;
  }

  protected String getDateOfChangeOverride() {
    return null;
  }

  protected void doDataSync(String queryString) {
    int totalRecords = 0;
    long lastEntityId = 0;
    boolean hasMoreResults = true;

    Stopwatch stopwatch = Stopwatch.createStarted();

    EntityManager entityManager = null;
    try {
      while (hasMoreResults) {
        entityManager = getEntityManagerFactory().createEntityManager();

        List<Long> collectedData =
            collectData(lastEntityId, queryString, entityManager);
        hasMoreResults = !collectedData.isEmpty();
        LOG.info("Time taken to read chunk : [{}]", stopwatch);

        if (CollectionUtils.isNotEmpty(collectedData)) {
          rabbitMqPublisher.publishToBroker(
              collectedData.stream().map(id -> String.valueOf(id)).collect(
                  Collectors.toList()));
          lastEntityId = collectedData.get(collectedData.size() - 1);
          totalRecords += collectedData.size();
        }
        stopwatch.reset().start();

        entityManager.close();
        stopwatch.reset().start();
      }
      LOG.info("Sync job [{}] finished. Total time taken {} for processing [{}] records",
          getJobName(), mainStopWatch.stop(), totalRecords);
      mainStopWatch = null;
      publishJobexecutionEvent(
          new JobExecutionEvent(this, getSuccessMessage(Optional.ofNullable(getJobName()))));
    } finally {
      mainStopWatch = null;

      if (entityManager != null && entityManager.isOpen()) {
        entityManager.close();
      }
    }
  }
}
