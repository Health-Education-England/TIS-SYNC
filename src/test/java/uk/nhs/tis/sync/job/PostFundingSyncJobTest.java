package uk.nhs.tis.sync.job;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
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
import javax.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PostFundingSyncJobTest {

  @Mock
  private EntityManager entityManager;
  @Mock
  private TypedQuery<PostFunding> query;

  private PostFundingSyncJob postFundingSyncJob;

  @BeforeEach
  public void setUp() {
    postFundingSyncJob = new PostFundingSyncJob();
  }

  @Test
  void testShouldWorkWithBuildQueryForDateMethod() {
    LocalDate dateOfChange = LocalDate.now();
    String expectedQuery = "SELECT DISTINCT p.id FROM Post p "
        + " JOIN ( "
        + " SELECT postId "
        + "  FROM PostFunding "
        + "      WHERE postId > :lastPostId "
        + "      AND startDate IS NOT NULL "
        + "      AND (endDate = '" + dateOfChange.minusDays(1)
        .format(DateTimeFormatter.ISO_LOCAL_DATE) + "' OR endDate IS NULL) "
        + " GROUP BY postId "
        + " ) pf ON p.id = pf.postId "
        + " ORDER BY p.id LIMIT " + PostFundingSyncJob.DEFAULT_PAGE_SIZE + " ";

    String actualQuery = postFundingSyncJob.buildQueryForDate(dateOfChange);
    assertThat(expectedQuery, is(actualQuery));
  }

  @Test
  public void testShouldConvertDataWithNoPostsAndFundings() {
    List<Long> entityData = new ArrayList<>();
    Set<Post> entitiesToSave = new HashSet<>();

    int result = postFundingSyncJob.convertData(entitiesToSave, entityData, entityManager);

    // No entities removed
    assertThat(result, is(0));
    // No entities added to save
    assertThat(entitiesToSave.size(), is(0));
  }

  @Test
  public void testShouldPassWhenAPostHasMultiplePostFundings() {
    Post post = new Post();
    post.setId(1L);
    PostFunding postFunding1 = new PostFunding();
    postFunding1.setId(999L);
    PostFunding postFunding2 = new PostFunding();
    postFunding2.setId(1000L);
    post.fundingStatus(Status.CURRENT);
    Set<PostFunding> postFundingSet = new HashSet<>(Arrays.asList(postFunding1, postFunding2));
    post.setFundings(postFundingSet);

    when(entityManager.find(eq(Post.class), anyLong())).thenReturn(post);
    when(entityManager.createQuery(anyString(), eq(PostFunding.class))).thenReturn(query);
    when(query.setParameter(anyString(), any())).thenReturn(query);
    List<PostFunding> mockPostFundings = Arrays.asList(new PostFunding(), new PostFunding());
    when(query.getResultList()).thenReturn(mockPostFundings);

    List<Long> entityData = new ArrayList<>();
    entityData.add(1L);
    Set<Post> entitiesToSave = new HashSet<>();

    int result = postFundingSyncJob.convertData(entitiesToSave, entityData, entityManager);

    assertThat(entitiesToSave.size(), is(1));
    assertThat(result, is(0));
    assertThat(entitiesToSave.contains(post), is(true));
    assertThat(post.getFundingStatus(), is(Status.CURRENT));
  }

  @Test
  public void testJobShouldChangeCurrentPostFundingToInactive() {
    Post post = new Post();
    post.setId(1L);
    PostFunding postFunding = new PostFunding();
    postFunding.setId(999L);
    post.fundingStatus(Status.CURRENT);
    Set<PostFunding> postFundingSet = new HashSet<>(Arrays.asList(postFunding));
    post.setFundings(postFundingSet);

    when(entityManager.find(eq(Post.class), anyLong())).thenReturn(post);
    when(entityManager.createQuery(anyString(), eq(PostFunding.class))).thenReturn(query);
    when(query.setParameter(anyString(), any())).thenReturn(query);
    List<PostFunding> mockPostFundings = Collections.singletonList(new PostFunding());
    when(query.getResultList()).thenReturn(mockPostFundings);

    List<Long> entityData = Arrays.asList(1L);

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
