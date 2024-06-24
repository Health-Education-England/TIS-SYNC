package uk.nhs.tis.sync.job;

import static uk.nhs.tis.sync.job.TrustAdminSyncJobTemplate.LAST_ENTITY_ID;

import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.service.model.Post;
import com.transformuk.hee.tis.tcs.service.model.PostFunding;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
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
@ManagedResource(objectName = "sync.mbean:name=PostFundingStatusSyncJob",
    description = "Job for updating funding status for posts")
@Slf4j
public class PostFundingStatusSyncJob extends CommonSyncJobTemplate<Post> {

  private static final String BASE_QUERY = " SELECT postId "
      + "  FROM PostFunding "
      + "  WHERE postId > :lastPostId "
      + "      AND startDate IS NOT NULL "
      + "      AND endDate = ':endDate' "
      + " GROUP BY postId "
      + " ORDER BY postId LIMIT :pageSize ";

  public PostFundingStatusSyncJob(EntityManagerFactory entityManagerFactory,
      @Autowired(required = false) ApplicationEventPublisher applicationEventPublisher) {
    super(entityManagerFactory, applicationEventPublisher);
  }

  @Scheduled(cron = "${application.cron.postFundingStatusSyncJob}")
  @SchedulerLock(name = "postFundingStatusSyncJobTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(description = "Run sync of modifying the post funding status")
  public void postFundingStatusSyncJob() {
    runSyncJob(null);
  }

  public void run(String params) {
    postFundingStatusSyncJob();
  }

  protected String buildQueryForDate() {
    LocalDate today = LocalDate.now();
    String endDate = today.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
    return BASE_QUERY.replace(":endDate", endDate).replace(":pageSize", "" + getPageSize());
  }

  @Override
  protected String assembleQueryString(String option) {
    String queryString = buildQueryForDate();
    log.debug("Job will run with query:[{}]", queryString);
    return queryString;
  }

  @Override
  protected List<EntityData> collectData(Map<String, Long> ids, String queryString,
      EntityManager entityManager) {
    long lastPostId = ids.get(LAST_ENTITY_ID);
    Query query =
        entityManager.createNativeQuery(queryString).setParameter("lastPostId", lastPostId);

    List<BigInteger> resultList = query.getResultList();
    return resultList.stream().filter(Objects::nonNull).map(objArr ->
        new EntityData().entityId(objArr.longValue())
    ).collect(Collectors.toList());
  }

  @Override
  protected int convertData(Set<Post> entitiesToSave, List<EntityData> entityData,
      EntityManager entityManager) {
    int entities = entityData.size();
    LocalDate yesterday = LocalDate.now().minusDays(1);

    for (EntityData entity : entityData) {
      Post post = entityManager.find(Post.class, entity.getEntityId());

      if (post != null) {
        processPostFundingStatus(entitiesToSave, yesterday, post);
      }
    }
    return entities - entitiesToSave.size();
  }

  private void processPostFundingStatus(Set<Post> entitiesToSave, LocalDate yesterday, Post post) {
    Set<PostFunding> fundings = post.getFundings();
    if (!fundings.isEmpty()) {
      boolean allEndDatesYesterday = checkAllEndDatesYesterday(fundings, yesterday);
      if (allEndDatesYesterday) {
        post.setFundingStatus(Status.INACTIVE);
      }
      entitiesToSave.add(post);
    }
  }

  private boolean checkAllEndDatesYesterday(Set<PostFunding> fundings, LocalDate yesterday) {
    for (PostFunding funding : fundings) {
      if (!yesterday.equals(funding.getEndDate())) {
        return false;
      }
    }
    return true;
  }

  protected void handleData(Set<Post> dataToSave, EntityManager entityManager) {
    if (CollectionUtils.isNotEmpty(dataToSave)) {
      dataToSave.forEach(entityManager::persist);
      entityManager.flush();
    }
  }

  @Override
  protected Map<String, Long> initIds() {
    Map<String, Long> idMap = new HashMap<>();
    idMap.put(LAST_ENTITY_ID, 0L);
    return idMap;
  }

  @Override
  protected void updateIds(Map<String, Long> ids, List<EntityData> collectedData) {
    ids.put(LAST_ENTITY_ID, collectedData.get(collectedData.size() - 1).getEntityId());
  }

  @Override
  protected void deleteData() {
    // This method is intentionally left blank because
    // delete will not required.
  }
}
