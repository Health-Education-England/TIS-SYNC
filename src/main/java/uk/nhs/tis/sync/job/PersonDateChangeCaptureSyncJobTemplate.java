package uk.nhs.tis.sync.job;

import static uk.nhs.tis.sync.job.TrustAdminSyncJobTemplate.LAST_ENTITY_ID;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.nhs.tis.sync.model.EntityData;

/**
 * Abstract template for Jobs which sync data when start and end dates have passed.
 */
public abstract class PersonDateChangeCaptureSyncJobTemplate<T> extends CommonSyncJobTemplate<T> {

  private static final Logger LOG = LoggerFactory.getLogger(
      PersonDateChangeCaptureSyncJobTemplate.class);

  protected static final String FULL_SYNC_DATE_STR = "ANY";
  protected static final String NO_DATE_OVERRIDE = "NONE";

  protected String dateOfChangeOverride;

  protected abstract String buildQueryForDate(LocalDate dateOfChange);

  protected abstract void handleData(Set<T> dataToSave, EntityManager entityManager);

  @Override
  protected String assembleQueryString(String dateOption) {
    LocalDate dateOfChange = magicallyGetDateOfChanges(dateOption);
    String queryString = buildQueryForDate(dateOfChange);
    LOG.debug("Job will run with query:[{}]", queryString);
    return queryString;
  }

  protected void deleteData() {
  }

  @Override
  protected List<EntityData> collectData(Map<String, Long> ids, String queryString,
      EntityManager entityManager) {
    long lastPersonId = ids.get(LAST_ENTITY_ID);
    Query query =
        entityManager.createNativeQuery(queryString).setParameter("lastPersonId", lastPersonId);

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

  private LocalDate magicallyGetDateOfChanges(String dateToUse) {
    if (StringUtils.equalsIgnoreCase(dateToUse, NO_DATE_OVERRIDE)) {
      dateToUse = this.dateOfChangeOverride;
    }
    if (StringUtils.isEmpty(dateToUse)) {
      return LocalDate.now();
    }
    if (FULL_SYNC_DATE_STR.equalsIgnoreCase(dateToUse)) {
      return null;
    }
    return LocalDate.parse(dateToUse);
  }
}
