package uk.nhs.tis.sync.job;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.service.model.Post;
import com.transformuk.hee.tis.tcs.service.model.PostFunding;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import uk.nhs.tis.sync.model.EntityData;

@ExtendWith(MockitoExtension.class)
class PostFundingSyncJobTest {

  private static final int DEFAULT_PAGE_SIZE = 5000;
  @Mock
  ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private EntityManager entityManager;
  @Mock
  private EntityManagerFactory entityManagerFactory;

  private PostFundingSyncJob postFundingSyncJob;

  @BeforeEach
  void setUp() {
    postFundingSyncJob = new PostFundingSyncJob(entityManagerFactory, applicationEventPublisher);
    postFundingSyncJob.entityManagerFactory = entityManagerFactory;
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

    String actualQuery = postFundingSyncJob.buildQueryForDate();
    assertThat(expectedQuery, is(actualQuery));
  }

  @Test
  void testShouldConvertDataWithNoPostsAndFundings() {
    List<EntityData> entityData = new ArrayList<>();
    Set<Post> entitiesToSave = new HashSet<>();

    int result = postFundingSyncJob.convertData(entitiesToSave, entityData, entityManager);

    assertThat(result, is(0));
    assertThat(entitiesToSave.size(), is(0));
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

    int result = postFundingSyncJob.convertData(entitiesToSave, entityData, entityManager);

    assertThat(entitiesToSave.size(), is(1));
    assertThat(result, is(0));
    assertThat(entitiesToSave.contains(post), is(true));
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
    int result = postFundingSyncJob.convertData(entitiesToSave, entityData, entityManager);

    assertThat(result, is(0));
    assertThat(entitiesToSave.contains(post), is(true));
    assertThat(post.getFundingStatus(), is(Status.INACTIVE));
  }

  @Test
  void testShouldBeSuccessfulWithHandleDataMethod() {
    Post post = new Post();
    Set<Post> dataToSave = new HashSet<>();
    dataToSave.add(post);
    postFundingSyncJob.handleData(dataToSave, entityManager);

    verify(entityManager, times(1)).persist(post);
    verify(entityManager, times(1)).flush();
  }
}
