package uk.nhs.tis.sync.job;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * This abstract class serves as a template for implementing post funding synchronization jobs
 * that collects data from a database using native SQL queries.
 *
 * @param <T> the type of data objects being synchronized
 */
public abstract class PostFundingSyncJobTemplate<T> extends
    PersonDateChangeCaptureSyncJobTemplate<T> {

  @Override
  protected List<Long> collectData(long lastPostId, String queryString,
      EntityManager entityManager) {
    Query query =
        entityManager.createNativeQuery(queryString).setParameter("lastPostId", lastPostId);

    List<BigInteger> resultList = query.getResultList();
    return resultList.stream().filter(Objects::nonNull)
        .map(result -> Long.parseLong(result.toString()))
        .collect(Collectors.toList());
  }
}
