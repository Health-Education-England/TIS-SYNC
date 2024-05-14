package uk.nhs.tis.sync.job;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.service.model.Post;
import com.transformuk.hee.tis.tcs.service.model.PostFunding;
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
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.common.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.event.JobExecutionEvent;


/**
 * Job for updating funding status for posts. This class extends the
 * PersonDateChangeCaptureSyncJobTemplate and provides functionality to synchronize post funding
 * status based on specified criteria.
 */
@Component
@ManagedResource(objectName = "sync.mbean:name=PostFundingSyncJob",
    description = "Job for updating funding status for posts")
public class PostFundingSyncJob implements RunnableJob {

  private static final String JOB_NAME = "Post funding sync job";
  private static final Logger LOG = LoggerFactory.getLogger(PostFundingSyncJob.class);
  private static final int FIFTEEN_MIN = 15 * 60 * 1000;
  private static final int DEFAULT_PAGE_SIZE = 5000;

  @Autowired
  protected EntityManagerFactory entityManagerFactory;

  private static final String BASE_QUERY = " SELECT postId "
      + "  FROM PostFunding "
      + "      WHERE postId > :lastPostId "
      + "      AND startDate IS NOT NULL "
      + "      AND (endDate = ':endDate' OR endDate IS NULL) "
      + " GROUP BY postId "
      + " ORDER BY postId LIMIT :pageSize ";

  @Autowired(required = false)
  protected ApplicationEventPublisher applicationEventPublisher;
  private Stopwatch mainStopWatch;

  @Scheduled(cron = "${application.cron.postFundingSyncJob}")
  @SchedulerLock(name = "postFundingSyncJobTask", lockAtLeastFor = FIFTEEN_MIN, lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(description = "Run sync of modifying the post funding status")
  public void postFundingSyncJob() {
    runSyncJob();
  }

  @Override
  @ManagedOperation(description = "Is the Post funding sync job currently running")
  public boolean isCurrentlyRunning() {
    return mainStopWatch != null;
  }

  @Override
  public void run(@Nullable String params) {
    postFundingSyncJob();
  }

  protected void runSyncJob() {
    if (isCurrentlyRunning()) {
      LOG.info("Sync job [{}] already running, exiting this execution", JOB_NAME);
      return;
    }
    CompletableFuture.runAsync(() -> {
      doDataSync();
    });
  }

  private void doDataSync() {
    String queryString = buildQueryForDate();
    int skipped = 0;
    int totalRecords = 0;
    long lastEntityId = 0;
    boolean hasMoreResults = true;
    Set<Post> dataToSave = Sets.newHashSet();
    LOG.debug("Job will run with query:[{}]", queryString);

    publishJobexecutionEvent(new JobExecutionEvent(this, "Sync [" + getJobName() + "] started."));
    LOG.info("Sync [{}] started", getJobName());
    mainStopWatch = Stopwatch.createStarted();
    Stopwatch stopwatch = Stopwatch.createStarted();

    EntityManager entityManager = null;
    EntityTransaction transaction = null;

    try {
      while (hasMoreResults) {
        entityManager = this.entityManagerFactory.createEntityManager();
        transaction = entityManager.getTransaction();
        transaction.begin();
        List<Long> collectedData =
            collectDataFromTheLastPostId(lastEntityId, queryString, entityManager);
        hasMoreResults = !collectedData.isEmpty();
        LOG.info("Time taken to read chunk : [{}]", stopwatch);
        if (CollectionUtils.isNotEmpty(collectedData)) {
          lastEntityId = collectedData.get(collectedData.size() - 1);
          totalRecords += collectedData.size();
          skipped += convertData(dataToSave, collectedData, entityManager);
        }
        stopwatch.reset().start();
        handleData(dataToSave, entityManager);
        LOG.debug("Collected {} records and attempted to process {}.", collectedData.size(),
            dataToSave.size());
        dataToSave.clear();
        transaction.commit();
        entityManager.close();
        LOG.info("Time taken to save/handle chunk : [{}]", stopwatch);
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

  private String getSuccessMessage(Optional<String> jobName) {
    return "Sync [" + jobName.orElse(getJobName()) + "] completed successfully.";
  }

  private void publishJobexecutionEvent(JobExecutionEvent event) {
    if (applicationEventPublisher != null) {
      applicationEventPublisher.publishEvent(event);
    }
  }

  protected String buildQueryForDate() {
    LocalDate today = LocalDate.now();
    String endDate = today.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
    return BASE_QUERY.replace(":endDate", endDate).replace(":pageSize", "" + DEFAULT_PAGE_SIZE);
  }

  protected String getJobName() {
    return this.getClass().getSimpleName();
  }

  protected int convertData(Set<Post> entitiesToSave, List<Long> entityData,
      EntityManager entityManager) {
    int entities = entityData.size();
    for (Long id : entityData) {
      Post post = entityManager.find(Post.class, id);

      if (post != null) {
        Set<PostFunding> fundings = post.getFundings();

        if (fundings.size() > 1) {
          entitiesToSave.add(post);
        } else if (fundings.size() == 1) {
          post.setFundingStatus(Status.INACTIVE);
          entitiesToSave.add(post);
        }
      }
    }
    return entities - entitiesToSave.size();
  }

  protected void handleData(Set<Post> dataToSave, EntityManager entityManager) {
    if (CollectionUtils.isNotEmpty(dataToSave)) {
      dataToSave.forEach(entityManager::persist);
      entityManager.flush();
    }
  }

  protected List<Long> collectDataFromTheLastPostId(long lastPostId, String queryString,
      EntityManager entityManager) {
    Query query =
        entityManager.createNativeQuery(queryString).setParameter("lastPostId", lastPostId);

    List<BigInteger> resultList = query.getResultList();
    return resultList.stream().filter(Objects::nonNull)
        .map(result -> Long.parseLong(result.toString()))
        .collect(Collectors.toList());
  }
}
