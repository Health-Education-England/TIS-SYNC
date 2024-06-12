package uk.nhs.tis.sync.job;

import static uk.nhs.tis.sync.job.TrustAdminSyncJobTemplate.LAST_ENTITY_ID;

import java.math.BigInteger;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import uk.nhs.tis.sync.model.EntityData;

/**
 * Abstract template for post funding sync job which sets post funding status
 *
 * @param <T> the type of entity passed to target
 */
@Slf4j
public abstract class PostFundingSyncJobTemplate<T> extends CommonSyncJobTemplate<T> {

  protected PostFundingSyncJobTemplate(EntityManagerFactory entityManagerFactory,
      @Autowired(required = false) ApplicationEventPublisher applicationEventPublisher) {
    super(entityManagerFactory, applicationEventPublisher);
  }

  protected abstract String buildQueryForDate();

  protected abstract void handleData(Set<T> dataToSave, EntityManager entityManager);

  public void run(String params) {
    runSyncJob(params);
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
  protected Map<String, Long> initIds() {
    Map<String, Long> idMap = new HashMap<>();
    idMap.put(LAST_ENTITY_ID, 0L);
    return idMap;
  }

  protected void updateIds(Map<String, Long> ids, List<EntityData> collectedData) {
    ids.put(LAST_ENTITY_ID, collectedData.get(collectedData.size() - 1).getEntityId());
  }

  @Override
  protected void deleteData() {
  }
}
