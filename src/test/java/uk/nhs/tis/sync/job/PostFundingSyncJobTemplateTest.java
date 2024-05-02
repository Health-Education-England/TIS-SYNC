package uk.nhs.tis.sync.job;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.*;
import org.springframework.context.ApplicationEventPublisher;
import uk.nhs.tis.sync.event.JobExecutionEvent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.ArgumentCaptor;

public class PostFundingSyncJobTemplateTest {

  @Mock
  private EntityManager entityManager;

  @Mock
  private EntityManagerFactory entityManagerFactory;

  @Mock
  private EntityTransaction transaction;

  @Mock
  private Query query;

  @InjectMocks
  private ConcretePostFundingSyncJobTemplate postFundingSyncJobTemplate;

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

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
    postFundingSyncJobTemplate = new ConcretePostFundingSyncJobTemplate();
    postFundingSyncJobTemplate.entityManagerFactory = entityManagerFactory;

    when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
    when(entityManager.getTransaction()).thenReturn(transaction);
    when(transaction.isActive()).thenReturn(false);
  }

  @Test
  public void testGetFailureMessage() {
    String errorMessage = "Failed!";
    Throwable exception = new RuntimeException(errorMessage);
    assertEquals("<!channel> Sync [TestJob] failed with exception [" + errorMessage + "].", postFundingSyncJobTemplate.getFailureMessage(Optional.of("TestJob"), exception));
    assertEquals("<!channel> Sync [PostFundingSyncJobTemplate] failed with exception [" + errorMessage + "].", postFundingSyncJobTemplate.getFailureMessage(Optional.empty(), exception));
  }

  @Test
  public void testDoDataSync() throws ExecutionException, InterruptedException, NoSuchMethodException, InvocationTargetException, IllegalAccessException{

    String dateOption = "2025-05-01";
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
    when(entityManager.createQuery(queryString)).thenReturn(query);
    when(query.setParameter("date", dateOfChange)).thenReturn(query);
    when(entityManager.createNativeQuery(any())).thenReturn(query);
    when(query.setParameter(eq("lastPostId"), any())).thenReturn(query);
    when(query.getResultList())
        .thenReturn(Arrays.asList(BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(3)))
        .thenReturn(new ArrayList<>()); // no more results

    // Use reflection to invoke the private method
    Method method = PostFundingSyncJobTemplate.class.getDeclaredMethod("doDataSync", String.class);
    method.setAccessible(true);
    method.invoke(postFundingSyncJobTemplate, dateOption);

    verify(query, times(2)).getResultList();
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
    protected void handleData(Set<Object> dataToSave, EntityManager entityManager) {

    }

    @Override
    public void run(@Nullable String params) {

    }
  }


}

