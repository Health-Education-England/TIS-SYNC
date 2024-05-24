package uk.nhs.tis.sync.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.CollectionUtils;
import uk.nhs.tis.sync.model.EntityData;
import javax.persistence.EntityManager;

/**
 * A template for trust admin sync jobs.
 *
 * @param <T> the entity type
 */
public abstract class TrustAdminSyncJobTemplate<T> extends CommonSyncJobTemplate<T> {

  protected static final String LAST_ENTITY_ID = "lastEntityId";
  protected static final String LAST_SITE_ID = "lastSiteId";

  public void run(String params) {
    runSyncJob(params);
  }

  protected void handleData(Set<T> dataToSave, EntityManager entityManager) {
    if (CollectionUtils.isNotEmpty(dataToSave)) {
      dataToSave.forEach(entityManager::persist);
      entityManager.flush();
    }
  }

  protected String assembleQueryString(String option) {
    return null;
  }

  @Override
  protected Map<String, Long> initIds() {
    Map<String, Long> idMap = new HashMap<>();
    idMap.put(LAST_ENTITY_ID, 0L);
    idMap.put(LAST_SITE_ID, 0L);
    return idMap;
  }

  protected void updateIds(Map<String, Long> ids, List<EntityData> collectedData) {
    ids.put(LAST_ENTITY_ID, collectedData.get(collectedData.size() - 1).getEntityId());
    ids.put(LAST_SITE_ID, collectedData.get(collectedData.size() - 1).getOtherId());
  }
}
