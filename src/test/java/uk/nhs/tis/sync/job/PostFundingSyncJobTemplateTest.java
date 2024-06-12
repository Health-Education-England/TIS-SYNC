package uk.nhs.tis.sync.job;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.tis.sync.job.TrustAdminSyncJobTemplate.LAST_ENTITY_ID;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.nhs.tis.sync.model.EntityData;

@ExtendWith(MockitoExtension.class)
class PostFundingSyncJobTemplateTest {

  @Mock
  Map<String, Long> ids;

  @Mock
  private EntityManagerFactory entityManagerFactoryMock;

  @Mock
  private EntityManager entityManagerMock;

  @Mock
  private List<EntityData> mockList;

  @Mock
  private Query query;

  @Mock
  private ApplicationEventPublisher applicationEventPublisherMock;
  private PostFundingSyncJobTemplateStub testObj;

  void instantiateTestJob(List<EntityData> data) {
    testObj = new PostFundingSyncJobTemplateStub(entityManagerFactoryMock,
        applicationEventPublisherMock, data);
  }

  @Test
  void testElapsedTimeAndIsCurrentlyRunning() {
    instantiateTestJob(null);
    boolean running = testObj.isCurrentlyRunning();
    String runningTime = testObj.elapsedTime();
    assertFalse(running);
    assertEquals("0s", runningTime);
  }

  @Test
  void testSucceededWhenExceptionIsThrown() {
    instantiateTestJob(mockList);
    when(entityManagerFactoryMock.createEntityManager())
        .thenThrow(new RuntimeException("Expected"));
    testObj.run("");
    await().pollDelay(1, TimeUnit.SECONDS).atLeast(1, TimeUnit.SECONDS).until(() -> true);
    assertThat("should be running", testObj.isCurrentlyRunning(), is(false));
  }

  @Test
  void testCollectData() {
    instantiateTestJob(mockList);
    String queryString = "SELECT id FROM posts WHERE id > :lastPostId";
    Long lastPostId = 1L;

    when(ids.get(LAST_ENTITY_ID)).thenReturn(1l);
    when(entityManagerMock.createNativeQuery(queryString)).thenReturn(query);
    when(query.setParameter("lastPostId", lastPostId)).thenReturn(query);

    List<BigInteger> mockResultList = Arrays.asList(BigInteger.valueOf(2L), BigInteger.valueOf(3L));
    when(query.getResultList()).thenReturn(mockResultList);

    List<EntityData> result = testObj.collectData(ids, queryString, entityManagerMock);

    verify(entityManagerMock).createNativeQuery(queryString);
    verify(query).setParameter("lastPostId", lastPostId);
    verify(query).getResultList();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(Long.valueOf(2L), result.get(0).getEntityId());
    assertEquals(Long.valueOf(3L), result.get(1).getEntityId());
  }

  static class PostFundingSyncJobTemplateStub extends PostFundingSyncJobTemplate<Object> {

    private final EntityManagerFactory entityManagerFactoryMock;
    private final List<EntityData> collectedData;

    public PostFundingSyncJobTemplateStub(EntityManagerFactory entityManagerFactoryMock,
        ApplicationEventPublisher applicationEventPublisherMock,
        List<EntityData> collectedData) {
      super(entityManagerFactoryMock, applicationEventPublisherMock);
      this.entityManagerFactoryMock = entityManagerFactoryMock;
      this.collectedData = collectedData;
    }

    @Override
    protected int convertData(Set<Object> entitiesToSave, List<EntityData> entityData,
        EntityManager entityManager) {
      entityData.forEach(o -> entitiesToSave.add(new Object()));
      return 0;
    }

    @Override
    protected String buildQueryForDate() {
      return "";
    }

    @Override
    protected void handleData(Set<Object> dataToSave, EntityManager entityManager) {
    }
  }
}
