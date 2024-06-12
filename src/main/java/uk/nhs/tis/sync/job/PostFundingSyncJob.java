package uk.nhs.tis.sync.job;

import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.service.model.Post;
import com.transformuk.hee.tis.tcs.service.model.PostFunding;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
 * Job for updating funding status for posts. This class extends the
 * PersonDateChangeCaptureSyncJobTemplate and provides functionality to synchronize post funding
 * status based on specified criteria.
 */
@Component
@ManagedResource(objectName = "sync.mbean:name=PostFundingSyncJob",
    description = "Job for updating funding status for posts")
@Slf4j
public class PostFundingSyncJob extends PostFundingSyncJobTemplate<Post> {

  private static final String JOB_NAME = "Post funding sync job";
  private static final int FIFTEEN_MIN = 15 * 60 * 1000;
  private static final int DEFAULT_PAGE_SIZE = 5000;
  private static final String BASE_QUERY = " SELECT postId "
      + "  FROM PostFunding "
      + "      WHERE postId > :lastPostId "
      + "      AND startDate IS NOT NULL "
      + "      AND (endDate = ':endDate' OR endDate IS NULL) "
      + " GROUP BY postId "
      + " ORDER BY postId LIMIT :pageSize ";

  @Autowired
  protected EntityManagerFactory entityManagerFactory;

  @Autowired(required = false)
  protected ApplicationEventPublisher applicationEventPublisher;

  public PostFundingSyncJob(EntityManagerFactory entityManagerFactory,
      @Autowired(required = false) ApplicationEventPublisher applicationEventPublisher) {
    super(entityManagerFactory, applicationEventPublisher);
  }

  @Scheduled(cron = "${application.cron.postFundingSyncJob}")
  @SchedulerLock(name = "postFundingSyncJobTask", lockAtLeastFor = FIFTEEN_MIN, lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(description = "Run sync of modifying the post funding status")
  public void postFundingSyncJob() {
    runSyncJob(null);
  }

  @Override
  protected String buildQueryForDate() {
    LocalDate today = LocalDate.now();
    String endDate = today.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
    return BASE_QUERY.replace(":endDate", endDate).replace(":pageSize", "" + DEFAULT_PAGE_SIZE);
  }

  @Override
  protected int convertData(Set<Post> entitiesToSave, List<EntityData> entityData,
      EntityManager entityManager) {
    int entities = entityData.size();
    for (EntityData entity : entityData) {
      Post post = entityManager.find(Post.class, entity.getEntityId());

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
}
