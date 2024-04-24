package uk.nhs.tis.sync.job;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.service.model.Post;
import com.transformuk.hee.tis.tcs.service.model.PostFunding;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class PostFundingSyncJobTest {

  @Mock
  private EntityManager entityManager;

  @Mock
  private ObjectMapper objectMapper;

  private PostFundingSyncJob postFundingSyncJob;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    postFundingSyncJob = new PostFundingSyncJob(objectMapper);
  }

  @Test
  void testBuildQueryForDate() {
    LocalDate dateOfChange = LocalDate.now();
    String expectedQuery =
        "SELECT DISTINCT p.id FROM Post p JOIN ( SELECT postId FROM PostFunding WHERE startDate IS NOT NULL AND (endDate = '"
            + dateOfChange.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
            + "' OR endDate IS NULL) GROUP BY postId HAVING COUNT(id) > 0) pf ON p.id = pf.postId ORDER BY p.id LIMIT "
            + PostFundingSyncJob.DEFAULT_PAGE_SIZE;
    String actualQuery = postFundingSyncJob.buildQueryForDate(dateOfChange);
    assertEquals(expectedQuery, actualQuery);
  }

  @Test
  void testConvertData() {
    Post post = new Post();
    post.setId(1L);
    PostFunding postFunding = new PostFunding();
    postFunding.setId(999L);
    post.fundingStatus(Status.CURRENT);
    Set<PostFunding> postFundingSet = new HashSet<>(Arrays.asList(postFunding));
    post.setFundings(postFundingSet);

    when(entityManager.find(eq(Post.class), anyLong())).thenReturn(post);

    Set<Post> entitiesToSave = new HashSet<>();
    List<Long> entityData = Arrays.asList(1L);
    int result = postFundingSyncJob.convertData(entitiesToSave, entityData, entityManager);

    assertEquals(0, result);
    assertTrue(entitiesToSave.contains(post));
    assertEquals(Status.INACTIVE, post.getFundingStatus());
  }

  @Test
  void testPostFundingWillRemainCurrentWithMulitiplePostFunding() {
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

    Set<Post> entitiesToSave = new HashSet<>();
    List<Long> entityData = Arrays.asList(1L);
    int result = postFundingSyncJob.convertData(entitiesToSave, entityData, entityManager);

    assertEquals(0, result);
    assertTrue(entitiesToSave.contains(post));
    assertEquals(Status.CURRENT, post.getFundingStatus());
  }

  @Test
  void testHandleData() {
    Post post = new Post();
    Set<Post> dataToSave = new HashSet<>();
    dataToSave.add(post);

    postFundingSyncJob.handleData(dataToSave, entityManager);

    verify(entityManager, times(1)).persist(post);
    verify(entityManager, times(1)).flush();
  }
}
