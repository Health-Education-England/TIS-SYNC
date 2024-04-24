package uk.nhs.tis.sync.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.service.model.Post;
import com.transformuk.hee.tis.tcs.service.model.PostFunding;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.EntityManager;
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
public class PostFundingSyncJob extends PersonDateChangeCaptureSyncJobTemplate<Post> {

  private static final Logger LOG = LoggerFactory.getLogger(PostFundingSyncJob.class);

  private static final String BASE_QUERY = "SELECT DISTINCT p.id FROM Post p"
      + " JOIN ( SELECT postId FROM PostFunding"
      + " WHERE startDate IS NOT NULL AND (endDate = ':endDate' OR endDate IS NULL)"
      + " GROUP BY postId"
      + " HAVING COUNT(id) > 0)"
      + " pf ON p.id = pf.postId ORDER BY p.id LIMIT :pageSize";

  private final ObjectMapper objectMapper;

  public PostFundingSyncJob(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void run(String params) {
    postFundingSyncJob();
  }

  @Scheduled(cron = "${application.cron.postFundingSyncJob}")
  @ManagedOperation(description = "update post funding status")
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
    entityData.stream()
        .map(id -> entityManager.find(Post.class, id))
        .filter(Objects::nonNull)
        .forEach(post -> {
          // check if the post has multiple post fundings
          Set<PostFunding> postFundings = post.getFundings();
          if (postFundings.size() > 1) {
            // if the post has multiple post fundings, do nothing
            entitiesToSave.add(post);
          } else if (postFundings.size() == 1) {
            // if the post has a single post funding, set its status to "INACTIVE"
            post.setFundingStatus(Status.INACTIVE);
            entitiesToSave.add(post);
          }
        });
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
