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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOG = LoggerFactory.getLogger(PostFundingSyncJob.class);

  private static final String BASE_QUERY = "SELECT DISTINCT p.id FROM Post p "
      + " JOIN ( "
      + " SELECT postId "
      + "  FROM PostFunding "
      + "      WHERE postId > :lastPostId "
      + "      AND startDate IS NOT NULL "
      + "      AND (endDate = ':endDate' OR endDate IS NULL) "
      + " GROUP BY postId "
      + " ) pf ON p.id = pf.postId "
      + " ORDER BY p.id LIMIT :pageSize ";

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
        // Explicitly load PostFunding entities without triggering further lazy loading
        List<PostFunding> fundings = entityManager.createQuery(
            "SELECT pf FROM PostFunding pf WHERE pf.post.id = :postId",
            PostFunding.class)
            .setParameter("postId", post.getId())
            .getResultList();

        if (fundings.size() > 1) {
          // If there are multiple fundings, save the current post
          entitiesToSave.add(post);
        } else if (fundings.size() == 1) {
          // If there's a single funding, update status to inactive and save
          PostFunding funding = fundings.get(0);
          LOG.info("Funding for the post {} is {} ", post.toString(), funding.toString());
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
