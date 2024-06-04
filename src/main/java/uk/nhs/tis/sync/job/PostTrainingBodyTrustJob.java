package uk.nhs.tis.sync.job;

import com.transformuk.hee.tis.tcs.service.model.Post;
import com.transformuk.hee.tis.tcs.service.model.PostTrust;
import com.transformuk.hee.tis.tcs.service.service.helper.SqlQuerySupplier;
import java.math.BigInteger;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.model.EntityData;

/**
 * This job runs on a daily basis and MUST be run after the PostEmployingBodyTrustJob.
 *
 * <p>Its purpose is to populate the PostTrust table with post ids and the linked
 * training body trust id.
 */
@Component
@ManagedResource(objectName = "sync.mbean:name=PostTrainingBodyTrustJob",
    description = "Service that links Post with Training Body Trusts")
@Slf4j
public class PostTrainingBodyTrustJob extends TrustAdminSyncJobTemplate<PostTrust> {

  private SqlQuerySupplier sqlQuerySupplier;

  public PostTrainingBodyTrustJob(EntityManagerFactory entityManagerFactory,
      ApplicationEventPublisher applicationEventPublisher,
      SqlQuerySupplier sqlQuerySupplier) {
    super(entityManagerFactory, applicationEventPublisher);
    this.sqlQuerySupplier = sqlQuerySupplier;
  }

  @Scheduled(cron = "${application.cron.postTrainingBodyTrustJob}")
  @SchedulerLock(name = "postTrustTrainingBodyScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(
      description = "Run sync of the PostTrust table with Post to Training Body Trust")
  public void postTrainingBodyTrustFullSync() {
    runSyncJob(null);
  }

  @Override
  protected void deleteData() {
    // This job runs after the PostEmployingBodyTrustJob and therefore shouldn't truncate the table
  }

  @Override
  protected List<EntityData> collectData(Map<String, Long> ids, String queryString,
                                         EntityManager entityManager) {
    long lastId = ids.get(LAST_ENTITY_ID);
    long lastTrainingBodyId = ids.get(LAST_SITE_ID);
    log.info("Querying with lastPostId: [{}] and lastTrainingBodyId: [{}]", lastId,
        lastTrainingBodyId);
    String postTrainingBodyQuery = sqlQuerySupplier.getQuery(SqlQuerySupplier.POST_TRAININGBODY);

    Query query = entityManager.createNativeQuery(postTrainingBodyQuery)
        .setParameter("lastId", lastId).setParameter("lastTrainingBodyId", lastTrainingBodyId)
        .setParameter("pageSize", getPageSize());

    List<Object[]> resultList = query.getResultList();
    List<EntityData> result = resultList.stream().filter(Objects::nonNull).map(objArr -> {
      EntityData entityData = new EntityData().entityId(((BigInteger) objArr[0]).longValue())
          .otherId(((BigInteger) objArr[1]).longValue());
      return entityData;
    }).collect(Collectors.toList());

    return result;
  }

  @Override
  protected int convertData(Set<PostTrust> entitiesToSave, List<EntityData> entityData,
                            EntityManager entityManager) {

    int skipped = 0;
    if (CollectionUtils.isNotEmpty(entityData)) {
      for (EntityData ed : entityData) {
        if (ed != null) {

          if (ed.getEntityId() != null) {
            PostTrust postTrust = new PostTrust();

            Post post = new Post();
            post.setId(ed.getEntityId());

            postTrust.setPost(post);
            postTrust.setTrustId(ed.getOtherId());

            entitiesToSave.add(postTrust);
          } else {
            skipped++;
          }
        }
      }
    }
    return skipped;
  }
}
