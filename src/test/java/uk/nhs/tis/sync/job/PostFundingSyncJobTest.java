package uk.nhs.tis.sync.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.service.model.Post;
import com.transformuk.hee.tis.tcs.service.model.PostFunding;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
public class PostFundingSyncJobTest {

  @Mock
  private EntityManager entityManager;

  @Mock
  private ObjectMapper objectMapper;

  @Mock
  private EntityManagerFactory entityManagerFactory;

  private PostFundingSyncJob postFundingSyncJob;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    postFundingSyncJob = new PostFundingSyncJob(objectMapper);
  }

  @Test
  public void testBuildQueryForDate() {
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
  public void testConvertData() {
    Post post = new Post();
    PostFunding postFunding = new PostFunding();
    post.fundingStatus(com.transformuk.hee.tis.tcs.api.enumeration.Status.CURRENT);
    Set<PostFunding> postFundingSet = new HashSet<>(Arrays.asList(postFunding));
    post.setFundings(postFundingSet);
    postFunding.setPost(post);

    when(entityManager.find(eq(Post.class), anyLong())).thenReturn(post);

    Set<Post> entitiesToSave = new HashSet<>();
    List<Long> entityData = Arrays.asList(1L);
    int result = postFundingSyncJob.convertData(entitiesToSave, entityData, entityManager);

    assertEquals(0, result);
    assertFalse(entitiesToSave.contains(post));
    assertEquals(com.transformuk.hee.tis.tcs.api.enumeration.Status.INACTIVE,
        postFunding.getPost().getFundingStatus());
  }

  @Test
  public void testHandleData() {
    Post post = new Post();
    Set<Post> dataToSave = new HashSet<>();
    dataToSave.add(post);

    postFundingSyncJob.handleData(dataToSave, entityManager);

    verify(entityManager, times(1)).persist(post);
    verify(entityManager, times(1)).flush();
  }
}

