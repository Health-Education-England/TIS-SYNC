package uk.nhs.tis.sync.job;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.tis.sync.job.TrustAdminSyncJobTemplate.LAST_ENTITY_ID;

import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.service.model.Post;
import com.transformuk.hee.tis.tcs.service.model.PostFunding;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.nhs.tis.sync.model.EntityData;

@ExtendWith(MockitoExtension.class)
class PostFundingStatusSyncJobTest {

  private static final int DEFAULT_PAGE_SIZE = 5000;
  @Mock
  ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private EntityManager entityManager;
  @Mock
  private EntityManagerFactory entityManagerFactory;
  @Mock
  Map<String, Long> ids;
  @Mock
  private Query query;

  private PostFundingStatusSyncJob postFundingStatusSyncJob;

  @BeforeEach
  void setUp() {
    postFundingStatusSyncJob = new PostFundingStatusSyncJob(entityManagerFactory, applicationEventPublisher);
  }

  @Test
  void testShouldWorkWithBuildQueryForDateMethod() {
    LocalDate dateOfChange = LocalDate.now();
    String expectedQuery = " SELECT postId "
        + "  FROM PostFunding "
        + "      WHERE postId > :lastPostId "
        + "      AND startDate IS NOT NULL "
        + "      AND (endDate = '" + dateOfChange.minusDays(1)
        .format(DateTimeFormatter.ISO_LOCAL_DATE) + "' OR endDate IS NULL) "
        + " GROUP BY postId "
        + " ORDER BY postId LIMIT " + DEFAULT_PAGE_SIZE + " ";

    String actualQuery = postFundingStatusSyncJob.buildQueryForDate();
    assertThat(actualQuery, is(expectedQuery));
  }

  @Test
  void testShouldConvertDataWithNoPostsAndFundings() {
    List<EntityData> entityData = new ArrayList<>();
    Set<Post> entitiesToSave = new HashSet<>();

    int result = postFundingStatusSyncJob.convertData(entitiesToSave, entityData, entityManager);

    assertThat(result, is(0));
    assertThat(entitiesToSave, is(empty()));
  }

  @Test
  void testShouldPassWhenAPostHasMultiplePostFundings() {
    Post post = new Post();
    post.setId(1L);

    PostFunding postFunding1 = new PostFunding();
    postFunding1.setId(999L);
    PostFunding postFunding2 = new PostFunding();
    postFunding2.setId(1000L);
    post.fundingStatus(Status.CURRENT);
    Set<PostFunding> postFundingSet = new HashSet<>(Arrays.asList(postFunding1, postFunding2));
    post.setFundings(postFundingSet);

    when(entityManager.find(Post.class, post.getId())).thenReturn(post);

    List<EntityData> entityData = new ArrayList<>();
    EntityData entity = new EntityData();
    entity.entityId(1L);
    entityData.add(entity);
    Set<Post> entitiesToSave = new HashSet<>();

    int result = postFundingStatusSyncJob.convertData(entitiesToSave, entityData, entityManager);

    assertThat(result, is(0));
    assertThat(entitiesToSave, contains(post));
    assertThat(post.getFundingStatus(), is(Status.CURRENT));
  }

  @Test
  void testJobShouldChangeCurrentPostFundingToInactive() {
    Post post = new Post();
    post.setId(1L);
    PostFunding postFunding = new PostFunding();
    postFunding.setId(999L);
    post.fundingStatus(Status.CURRENT);
    Set<PostFunding> postFundingSet = Collections.singleton(postFunding);
    post.setFundings(postFundingSet);

    when(entityManager.find(Post.class, post.getId())).thenReturn(post);

    List<EntityData> entityData = new ArrayList<>();
    EntityData entity = new EntityData();
    entity.entityId(1L);
    entityData.add(entity);

    Set<Post> entitiesToSave = new HashSet<>();
    int result = postFundingStatusSyncJob.convertData(entitiesToSave, entityData, entityManager);

    assertThat(result, is(0));
    assertThat(entitiesToSave, contains(post));
    assertThat(post.getFundingStatus(), is(Status.INACTIVE));
  }

  @Test
  void testShouldBeSuccessfulWithHandleDataMethod() {
    Post post = new Post();
    Set<Post> dataToSave = new HashSet<>();
    dataToSave.add(post);
    postFundingStatusSyncJob.handleData(dataToSave, entityManager);

    verify(entityManager, times(1)).persist(post);
    verify(entityManager).flush();
  }

  @Test
  void testElapsedTimeAndIsCurrentlyRunning() {
    boolean running = postFundingStatusSyncJob.isCurrentlyRunning();
    String runningTime = postFundingStatusSyncJob.elapsedTime();
    assertFalse(running);
    assertThat(runningTime, is("0s"));
  }

  @Test
  void testSucceededWhenExceptionIsThrown() {
    when(entityManagerFactory.createEntityManager())
        .thenThrow(new RuntimeException("Expected"));
    postFundingStatusSyncJob.run("");
    await()
        .atMost(5, TimeUnit.SECONDS)
        .until(() -> !postFundingStatusSyncJob.isCurrentlyRunning());
    assertThat("should be running", postFundingStatusSyncJob.isCurrentlyRunning(), is(false));
  }

  @Test
  void testCollectData() {
    String queryString = "SELECT id FROM posts WHERE id > :lastPostId";
    Long lastPostId = 1L;

    when(ids.get(LAST_ENTITY_ID)).thenReturn(1l);
    when(entityManager.createNativeQuery(queryString)).thenReturn(query);
    when(query.setParameter("lastPostId", lastPostId)).thenReturn(query);

    List<BigInteger> mockResultList = Arrays.asList(BigInteger.valueOf(2L), BigInteger.valueOf(3L));
    when(query.getResultList()).thenReturn(mockResultList);

    List<EntityData> result = postFundingStatusSyncJob.collectData(ids, queryString, entityManager);

    verify(entityManager).createNativeQuery(queryString);
    verify(query).setParameter("lastPostId", lastPostId);
    verify(query).getResultList();

    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(Long.valueOf(2L), result.get(0).getEntityId());
    assertEquals(Long.valueOf(3L), result.get(1).getEntityId());
  }
}
