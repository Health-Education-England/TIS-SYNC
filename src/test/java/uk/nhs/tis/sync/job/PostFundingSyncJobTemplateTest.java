package uk.nhs.tis.sync.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
class PostFundingSyncJobTemplateTest {

  @Mock
  private EntityManager entityManager;

  @Mock
  private EntityManagerFactory entityManagerFactory;

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
  void testGetFailureMessage() {
    String errorMessage = "Failed!";
    Throwable exception = new RuntimeException(errorMessage);
    assertEquals("<!channel> Sync [TestJob] failed with exception [" + errorMessage + "].",
        postFundingSyncJobTemplate.getFailureMessage(Optional.of("TestJob"), exception));
    assertEquals("<!channel> Sync [ConcretePostFundingSyncJobTemplate] failed with exception ["
            + errorMessage + "].",
        postFundingSyncJobTemplate.getFailureMessage(Optional.empty(), exception));
  }

  @Test
  void testCollectData() {
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
  void testPublishJobExecutionEvent() {
    postFundingSyncJobTemplate.applicationEventPublisher = applicationEventPublisher;

    JobExecutionEvent event = new JobExecutionEvent(this, "Test Job");

    postFundingSyncJobTemplate.publishJobexecutionEvent(event);

    ArgumentCaptor<JobExecutionEvent> eventCaptor = ArgumentCaptor
        .forClass(JobExecutionEvent.class);
    verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());

    JobExecutionEvent capturedEvent = eventCaptor.getValue();
    assertSame(event, capturedEvent);
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
