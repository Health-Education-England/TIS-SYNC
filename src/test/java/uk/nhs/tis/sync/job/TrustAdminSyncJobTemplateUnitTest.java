package uk.nhs.tis.sync.job;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.with;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.nhs.tis.sync.model.EntityData;

@RunWith(MockitoJUnitRunner.class)
public class TrustAdminSyncJobTemplateUnitTest {

  private TrustAdminSyncJobTemplateStub testObj;

  @Mock
  private EntityManagerFactory entityManagerFactoryMock;
  @Mock
  private EntityManager entityManagerMock;
  @Mock
  private EntityTransaction entityTransactionMock;

  @Mock
  private List<EntityData> mockList;

  public void instantiateJob(List<EntityData> data) {
    testObj = new TrustAdminSyncJobTemplateStub(entityManagerFactoryMock, data);
  }

  @Test
  public void runShouldPersistWhenDataReturnedFromQuery() {
    List<EntityData> data = Lists.newArrayList();
    for (int i = 0; i < 100; i++) {
      data.add(new EntityData().entityId((long) i).otherId((long) i));
    }
    instantiateJob(data);
    when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
    when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);

    testObj.run();

    verify(entityManagerMock, times(100)).persist(any());
    verify(entityManagerMock, times(2)).flush();
    verify(entityTransactionMock, times(2)).commit();
  }

  @Test
  public void testElapsedTimeAndRunningWhenNotRunning() {
    instantiateJob(null);
    //Combining as they are
    boolean running = testObj.isCurrentlyRunning();
    String runningTime = testObj.elapsedTime();
    assertFalse(running);
    assertEquals("0s", runningTime);
  }

  @Test
  public void testElapsedTimeAndRunningAndAddedInvokeWhenRunning() {
    instantiateJob(mockList);
    when(entityManagerFactoryMock.createEntityManager()).thenReturn(entityManagerMock);
    when(entityManagerMock.getTransaction()).thenReturn(entityTransactionMock);
    when(mockList.isEmpty()).thenAnswer(i -> {
      with().pollDelay(2, TimeUnit.SECONDS).atLeast(2, TimeUnit.SECONDS).until(() -> true);
      return true;
    });

    testObj.run("");
    with().pollDelay(1, TimeUnit.SECONDS).atLeast(1, TimeUnit.SECONDS).until(() -> true);
    boolean running = testObj.isCurrentlyRunning();
    String runningTime = testObj.elapsedTime();
    //rerun to check that we don't get a duplicate invocation
    testObj.run("");
    assertThat("should be running", running, is(true));
    assertNotEquals("should not be zero", "0s", runningTime);
  }

  @Test
  public void testCoverageBoostWhenExceptionThrown() {
    instantiateJob(mockList);
    when(entityManagerFactoryMock.createEntityManager())
        .thenThrow(new RuntimeException("Expected"));
    testObj.run("");
    await().pollDelay(1, TimeUnit.SECONDS).atLeast(1, TimeUnit.SECONDS).until(() -> true);
    assertThat("should be running", testObj.isCurrentlyRunning(), is(false));
  }

  static class TrustAdminSyncJobTemplateStub extends TrustAdminSyncJobTemplate<Object> {

    private final EntityManagerFactory entityManagerFactoryMock;
    private final List<EntityData> collectedData;
    private boolean firstCall = true;

    public TrustAdminSyncJobTemplateStub(EntityManagerFactory entityManagerFactoryMock,
        List<EntityData> collectedData) {
      this.entityManagerFactoryMock = entityManagerFactoryMock;
      this.collectedData = collectedData;
    }

    @Override
    protected String getJobName() {
      return null;
    }

    @Override
    protected int getPageSize() {
      return 10;
    }

    @Override
    protected EntityManagerFactory getEntityManagerFactory() {
      return this.entityManagerFactoryMock;
    }

    @Override
    protected void deleteData() {

    }

    @Override
    protected List<EntityData> collectData(int pageSize, long lastId, long lastSiteId,
        EntityManager entityManager) {
      if (firstCall) {
        firstCall = false;
        return this.collectedData;
      }
      return Lists.emptyList();
    }

    @Override
    protected int convertData(int skipped, Set<Object> entitiesToSave, List<EntityData> entityData,
        EntityManager entityManager) {
      entityData.forEach(o -> entitiesToSave.add(new Object()));
      return 0;
    }
  }
}
