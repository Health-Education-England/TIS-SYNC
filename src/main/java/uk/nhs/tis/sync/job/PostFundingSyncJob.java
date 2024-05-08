package uk.nhs.tis.sync.job;

import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.service.model.Post;
import com.transformuk.hee.tis.tcs.service.model.PostFunding;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job for updating funding status for posts. This class extends the
 * PersonDateChangeCaptureSyncJobTemplate and provides functionality to synchronize post funding
 * status based on specified criteria.
 */
@Component
@ManagedResource(objectName = "sync.mbean:name=PostFundingSyncJob",
    description = "Job for updating funding status for posts")
public class PostFundingSyncJob extends PostFundingSyncJobTemplate<Post> {

  private static final String BASE_QUERY = " SELECT postId "
      + "  FROM PostFunding "
      + "      WHERE postId > :lastPostId "
      + "      AND startDate IS NOT NULL "
      + "      AND (endDate = ':endDate' OR endDate IS NULL) "
      + " GROUP BY postId "
      + " ORDER BY postId LIMIT :pageSize ";

  @Override
  public void run(String params) {
    postFundingSyncJob();
  }

  @Scheduled(cron = "${application.cron.postFundingSyncJob}")
  @SchedulerLock(name = "postFundingScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(description = "Update post funding status")
  public void postFundingSyncJob() {
    super.runSyncJob(null);
  }

  @Override
  protected String buildQueryForDate(LocalDate dateOfChange) {
    String endDate = dateOfChange.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
    return BASE_QUERY.replace(":endDate", endDate).replace(":pageSize", "" + DEFAULT_PAGE_SIZE);
  }

  @Override
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

  @Override
  protected void handleData(Set<Post> dataToSave, EntityManager entityManager) {
    if (CollectionUtils.isNotEmpty(dataToSave)) {
      dataToSave.forEach(entityManager::persist);
      entityManager.flush();
    }
  }
}
