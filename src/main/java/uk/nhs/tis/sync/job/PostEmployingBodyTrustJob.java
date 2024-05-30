package uk.nhs.tis.sync.job;

import com.transformuk.hee.tis.tcs.service.model.Post;
import com.transformuk.hee.tis.tcs.service.model.PostTrust;
import com.transformuk.hee.tis.tcs.service.repository.PostTrustRepository;
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
 * This job runs on a daily basis and must be the first job that works on the PostTrust table as it
 * truncates it at the beginning.
 * <p>
 * Its purpose is to clear down the PostTrust table then populate it with post ids and the linked
 * employing body trust id
 */
@Component
@ManagedResource(objectName = "sync.mbean:name=PostEmployingBodyTrustJob",
    description = "Service that clears the PersonTrust table and links Post with Employing Body Trusts")
@Slf4j
public class PostEmployingBodyTrustJob extends TrustAdminSyncJobTemplate<PostTrust> {

  private PostTrustRepository postTrustRepository;

  private SqlQuerySupplier sqlQuerySupplier;

  public PostEmployingBodyTrustJob(EntityManagerFactory entityManagerFactory,
      ApplicationEventPublisher applicationEventPublisher, PostTrustRepository postTrustRepository,
      SqlQuerySupplier sqlQuerySupplier) {
    super(entityManagerFactory, applicationEventPublisher);
    this.postTrustRepository = postTrustRepository;
    this.sqlQuerySupplier = sqlQuerySupplier;
  }

  @Scheduled(cron = "${application.cron.postEmployingBodyTrustJob}")
  @SchedulerLock(name = "postTrustEmployingBodyScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(
      description = "Run sync of the PostTrust table with Post to Employing Body Trust")
  public void PostEmployingBodyTrustFullSync() {
    runSyncJob(null);
  }

  @Override
  protected void deleteData() {
    log.info("deleting all data");
    postTrustRepository.deleteAllInBatch();
    log.info("deleted all PostTrust data");
  }

  @Override
  protected List<EntityData> collectData(Map<String, Long> ids, String queryString,
                                         EntityManager entityManager) {
    long lastId = ids.get(LAST_ENTITY_ID);
    long lastEmployingBodyId = ids.get(LAST_SITE_ID);
    log.info("Querying with lastPostId: [{}] and lastEmployingBodyId: [{}]", lastId,
        lastEmployingBodyId);
    String postEmployingBodyQuery = sqlQuerySupplier.getQuery(SqlQuerySupplier.POST_EMPLOYINGBODY);

    Query query = entityManager.createNativeQuery(postEmployingBodyQuery)
        .setParameter("lastId", lastId).setParameter("lastEmployingBodyId", lastEmployingBodyId)
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
