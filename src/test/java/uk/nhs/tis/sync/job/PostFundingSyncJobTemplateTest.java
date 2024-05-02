package uk.nhs.tis.sync.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Stopwatch;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.nhs.tis.sync.event.JobExecutionEvent;

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

  @Mock
  ApplicationEventPublisher applicationEventPublisher;

  private ConcretePostFundingSyncJobTemplate postFundingSyncJobTemplate;

  @BeforeEach
  void setUp() {
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
  public void testMagicallyGetDateOfChanges()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    postFundingSyncJobTemplate.dateOfChangeOverride = LocalDate.now().toString();

    Method method = PostFundingSyncJobTemplate.class
        .getDeclaredMethod("magicallyGetDateOfChanges", String.class);
    method.setAccessible(true);

    LocalDate date = (LocalDate) method.invoke(postFundingSyncJobTemplate, "NONE");
    assertEquals(LocalDate.now(), date);

    date = (LocalDate) method.invoke(postFundingSyncJobTemplate, "ANY");
    assertNull(date);

    date = (LocalDate) method.invoke(postFundingSyncJobTemplate, "2023-01-01");
    assertEquals(LocalDate.of(2023, 1, 1), date);

    String dateToUse = "";
    date = (LocalDate) method.invoke(postFundingSyncJobTemplate, dateToUse);
    assertEquals(LocalDate.now(), date);
  }

  @Test
  public void testDoDataSync()
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    String dateOption = "2024-05-01";
    LocalDate dateOfChange = LocalDate.parse(dateOption);

    when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
    when(entityManager.getTransaction()).thenReturn(transaction);
    when(transaction.isActive()).thenReturn(false);
    when(entityManager.createNativeQuery(any())).thenReturn(query);
    when(query.setParameter(eq("lastPostId"), any())).thenReturn(query);
    when(query.getResultList())
        .thenReturn(
            Arrays.asList(BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3)))
        .thenReturn(new ArrayList<>());

    Method method = PostFundingSyncJobTemplate.class.getDeclaredMethod("doDataSync", String.class);
    method.setAccessible(true);
    method.invoke(postFundingSyncJobTemplate, dateOption);

    verify(query, times(2)).getResultList();
    verify(entityManagerFactory, times(2)).createEntityManager();
    verify(entityManager, times(2)).getTransaction();
    verify(transaction, times(2)).begin();
    verify(query, times(1)).setParameter("lastPostId", 0L);
    verify(query, times(2)).getResultList();
    verify(query, times(0)).setParameter("date", dateOfChange);
  }

  @Test
  public void testCollectData() {
    when(entityManager.createNativeQuery(anyString())).thenReturn(query);
    when(query.setParameter(anyString(), any())).thenReturn(query);
    when(query.getResultList())
        .thenReturn(Arrays.asList(BigInteger.ONE, BigInteger.valueOf(2), BigInteger.valueOf(3)));

    List<Long> collectedData = postFundingSyncJobTemplate
        .collectData(0L, "SELECT id FROM table WHERE id > :lastPostId", entityManager);

    assertNotNull(collectedData);
    assertEquals(3, collectedData.size());
    assertTrue(collectedData.contains(1L));
    assertTrue(collectedData.contains(2L));
    assertTrue(collectedData.contains(3L));
  }

  @Test
  public void testPublishJobExecutionEvent() {
    postFundingSyncJobTemplate.applicationEventPublisher = applicationEventPublisher;

    JobExecutionEvent event = new JobExecutionEvent(this, "Test Job");

    postFundingSyncJobTemplate.publishJobexecutionEvent(event);

    ArgumentCaptor<JobExecutionEvent> eventCaptor = ArgumentCaptor
        .forClass(JobExecutionEvent.class);
    verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());

    JobExecutionEvent capturedEvent = eventCaptor.getValue();
    assertSame(event, capturedEvent);
  }

  @Test
  public void testIsCurrentlyRunningWhenStopWatchIsNullReturnsFalse()
      throws NoSuchFieldException, IllegalAccessException {
    setMainStopWatch(postFundingSyncJobTemplate, null);

    boolean result = postFundingSyncJobTemplate.isCurrentlyRunning();
    assertFalse(result);
  }

  @Test
  public void testIsCurrentlyRunningWhenStopWatchIsNotNullReturnsTrue()
      throws NoSuchFieldException, IllegalAccessException {
    setMainStopWatch(postFundingSyncJobTemplate, Stopwatch.createStarted());

    boolean result = postFundingSyncJobTemplate.isCurrentlyRunning();
    assertTrue(result);
  }

  @Test
  public void testFinallyBlockExceptionHandlingOfDoDataSync() throws NoSuchMethodException {
    String dateOption = "2024-05-01";

    when(entityManagerFactory.createEntityManager()).thenReturn(entityManager);
    when(entityManager.getTransaction()).thenReturn(transaction);
    when(transaction.isActive()).thenReturn(true);

    doThrow(new RuntimeException("Simulated rollback failure")).when(transaction).rollback();

    Method method = PostFundingSyncJobTemplate.class.getDeclaredMethod("doDataSync", String.class);
    method.setAccessible(true);

    try {
      method.invoke(postFundingSyncJobTemplate, dateOption);
    } catch (Exception e) {}

    verify(transaction, times(1)).rollback();
  }

  private void setMainStopWatch(PostFundingSyncJobTemplate<?> postFundingSyncJobTemplate,
      Stopwatch stopWatch)
      throws NoSuchFieldException, IllegalAccessException {
    Field field = PostFundingSyncJobTemplate.class.getDeclaredField("mainStopWatch");
    field.setAccessible(true);
    field.set(postFundingSyncJobTemplate, stopWatch);
  }

  private static class ConcretePostFundingSyncJobTemplate extends
      PostFundingSyncJobTemplate<Object> {

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
