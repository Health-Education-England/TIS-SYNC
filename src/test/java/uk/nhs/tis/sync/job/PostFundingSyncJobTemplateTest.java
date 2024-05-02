package uk.nhs.tis.sync.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PostFundingSyncJobTemplateTest {

  @Mock
  private EntityManager entityManager;

  @Mock
  private EntityManagerFactory entityManagerFactory;

  @Mock
  private EntityTransaction transaction;

  @Mock
  private Query query;

  private ConcretePostFundingSyncJobTemplate postFundingSyncJobTemplate;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    postFundingSyncJobTemplate = new ConcretePostFundingSyncJobTemplate();
    postFundingSyncJobTemplate.entityManagerFactory = entityManagerFactory;

  }

  @Test
  public void testGetFailureMessage() {
    String errorMessage = "Failed!";
    Throwable exception = new RuntimeException(errorMessage);
    assertEquals("<!channel> Sync [TestJob] failed with exception [" + errorMessage + "].",
        postFundingSyncJobTemplate.getFailureMessage(Optional.of("TestJob"), exception));
    assertEquals("<!channel> Sync [ConcretePostFundingSyncJobTemplate] failed with exception ["
            + errorMessage + "].",
        postFundingSyncJobTemplate.getFailureMessage(Optional.empty(), exception));
  }

  @Test
  public void testDoDataSync() throws ExecutionException, InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException{
    String dateOption = "2024-05-01";
    LocalDate dateOfChange = LocalDate.parse(dateOption);
    String queryString = "SELECT DISTINCT p.id FROM Post p "
        + " JOIN ( "
        + " SELECT postId "
        + "  FROM PostFunding "
        + "      WHERE postId > :lastPostId "
        + "      AND startDate IS NOT NULL "
        + "      AND (endDate = ':endDate' OR endDate IS NULL) "
        + " GROUP BY postId "
        + " ) pf ON p.id = pf.postId "
        + " ORDER BY p.id LIMIT :pageSize ";

    when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
    when(entityManager.getTransaction()).thenReturn(transaction);
    when(transaction.isActive()).thenReturn(false);
    when(entityManager.createNativeQuery(any())).thenReturn(query);
    when(query.setParameter(eq("lastPostId"), any())).thenReturn(query);
    when(query.getResultList())
        .thenReturn(
            Arrays.asList(BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3)))
        .thenReturn(new ArrayList<>());

    // Use reflection to invoke the private method
    Method method = PostFundingSyncJobTemplate.class.getDeclaredMethod("doDataSync", String.class);
    method.setAccessible(true);
    method.invoke(postFundingSyncJobTemplate, dateOption);

    verify(query, times(2)).getResultList();
    verify(entityManagerFactory, times(2)).createEntityManager();
    verify(entityManager, times(2)).getTransaction();
    verify(transaction, times(2)).begin();
    verify(query, times(1)).setParameter("lastPostId", 0L);
    verify(query, times(2)).getResultList(); // Ensure getResultList is invoked twice
    verify(query, times(0)).setParameter("date", dateOfChange);
  }

  @Test
  public void testCollectData() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    when(query.setParameter(anyString(), any())).thenReturn(query);
    when(query.getResultList()).thenReturn(Arrays.asList(BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(3)));

    List<Long> collectedData = postFundingSyncJobTemplate.collectData(0L, "SELECT id FROM table WHERE id > :lastPostId", entityManager);

    assertNotNull(collectedData);
    assertEquals(3, collectedData.size());
    assertTrue(collectedData.contains(1L));
    assertTrue(collectedData.contains(2L));
    assertTrue(collectedData.contains(3L));
  }

  private static class ConcretePostFundingSyncJobTemplate extends PostFundingSyncJobTemplate<Object> {
    @Override
    protected int convertData(Set<Object> entitiesToSave, List<Long> entityData,
        EntityManager entityManager) {
      return 0;
    }
    @Override
    protected String buildQueryForDate(LocalDate dateOfChange) {
      return null;
    }
    @Override
    protected void handleData(Set<Object> dataToSave, EntityManager entityManager) { }
    @Override
    public void run(@Nullable String params) { }
  }
}
