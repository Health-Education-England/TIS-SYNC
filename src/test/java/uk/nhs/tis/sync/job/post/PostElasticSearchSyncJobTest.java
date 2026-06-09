/*
 * The MIT License (MIT)
 *
 * Copyright 2026 Crown Copyright (NHS England)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package uk.nhs.tis.sync.job.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.transformuk.hee.tis.tcs.service.job.post.PostView;
import com.transformuk.hee.tis.tcs.service.service.helper.SqlQuerySupplier;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.nhs.tis.sync.event.JobExecutionEvent;
import uk.nhs.tis.sync.service.PostElasticSearchService;
import uk.nhs.tis.sync.service.impl.PostViewRowMapper;

@ExtendWith(MockitoExtension.class)
class PostElasticSearchSyncJobTest {

  private static final int PAGE_SIZE = 2;

  @Mock
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  @Mock
  private SqlQuerySupplier sqlQuerySupplier;
  @Mock
  private ElasticsearchOperations elasticSearchOperations;
  @Mock
  private PostElasticSearchService postElasticSearchService;
  @Mock
  private PostViewRowMapper postViewRowMapper;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private IndexOperations postsIndexOperations;
  @Mock
  private IndexOperations postViewIndexOperations;
  @Mock
  private Document mapping;

  private PostElasticSearchSyncJob job;

  @BeforeEach
  void setUp() {
    job = new PostElasticSearchSyncJob(
        namedParameterJdbcTemplate,
        sqlQuerySupplier,
        elasticSearchOperations,
        postElasticSearchService,
        postViewRowMapper,
        applicationEventPublisher,
        PAGE_SIZE
    );
  }

  @Test
  void shouldRunPostElasticSearchSyncJobSuccessfully() {
    PostView firstPost = postView(100L);
    PostView secondPost = postView(200L);

    when(elasticSearchOperations.indexOps(any(IndexCoordinates.class)))
        .thenReturn(postsIndexOperations);
    when(elasticSearchOperations.indexOps(PostView.class))
        .thenReturn(postViewIndexOperations);

    when(postsIndexOperations.createMapping(PostView.class)).thenReturn(mapping);

    when(sqlQuerySupplier.getQuery(SqlQuerySupplier.POST_VIEW))
        .thenReturn(postEsSql());

    when(namedParameterJdbcTemplate.query(
        anyString(),
        any(MapSqlParameterSource.class),
        same(postViewRowMapper)))
        .thenReturn(Lists.newArrayList(firstPost, secondPost))
        .thenReturn(Collections.emptyList());

    job.runSynchronously();

    verify(postsIndexOperations).delete();
    verify(postsIndexOperations).create();
    verify(postsIndexOperations).createMapping(PostView.class);
    verify(postsIndexOperations).putMapping(mapping);

    verify(postElasticSearchService).saveDocuments(Lists.newArrayList(firstPost, secondPost));

    verify(postViewIndexOperations).refresh();

    verify(applicationEventPublisher, times(2)).publishEvent(any(JobExecutionEvent.class));

    assertThat(job.isCurrentlyRunning()).isFalse();
    assertThat(job.elapsedTime()).isEqualTo("0s");
  }

  @Test
  void shouldCollectDataUsingExpectedPostEsSqlPlaceholdersAndPagination() {
    when(elasticSearchOperations.indexOps(any(IndexCoordinates.class)))
        .thenReturn(postsIndexOperations);
    when(elasticSearchOperations.indexOps(PostView.class))
        .thenReturn(postViewIndexOperations);

    when(postsIndexOperations.createMapping(PostView.class)).thenReturn(mapping);

    when(sqlQuerySupplier.getQuery(SqlQuerySupplier.POST_VIEW))
        .thenReturn(postEsSql());

    when(namedParameterJdbcTemplate.query(
        anyString(),
        any(MapSqlParameterSource.class),
        same(postViewRowMapper)))
        .thenReturn(Collections.singletonList(postView(100L)))
        .thenReturn(Collections.emptyList());

    job.runSynchronously();

    ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

    verify(namedParameterJdbcTemplate, times(2)).query(
        sqlCaptor.capture(),
        any(MapSqlParameterSource.class),
        same(postViewRowMapper)
    );

    List<String> executedSql = sqlCaptor.getAllValues();

    assertThat(executedSql.get(0)).contains("ORDER BY id DESC");
    assertThat(executedSql.get(0)).contains("limit 2 offset 0");

    assertThat(executedSql.get(1)).contains("ORDER BY id DESC");
    assertThat(executedSql.get(1)).contains("limit 2 offset 2");

    assertThat(executedSql.get(0)).doesNotContain("WHERECLAUSE");
    assertThat(executedSql.get(0)).doesNotContain("ORDERBYCLAUSE");
    assertThat(executedSql.get(0)).doesNotContain("LIMITCLAUSE");
  }

  @Test
  void shouldPublishFailureEventAndClearRunningStateWhenExceptionOccurs() {
    RuntimeException failure = new RuntimeException("Database unavailable");

    when(elasticSearchOperations.indexOps(any(IndexCoordinates.class)))
        .thenReturn(postsIndexOperations);

    when(sqlQuerySupplier.getQuery(SqlQuerySupplier.POST_VIEW))
        .thenReturn(postEsSql());

    when(postsIndexOperations.createMapping(PostView.class)).thenReturn(mapping);

    when(namedParameterJdbcTemplate.query(
        anyString(),
        any(MapSqlParameterSource.class),
        same(postViewRowMapper)))
        .thenThrow(failure);

    job.runSynchronously();

    verify(applicationEventPublisher, times(2)).publishEvent(any(JobExecutionEvent.class));

    verify(postElasticSearchService, never()).saveDocuments(anyList());

    assertThat(job.isCurrentlyRunning()).isFalse();
    assertThat(job.elapsedTime()).isEqualTo("0s");
  }

  @Test
  void shouldReturnZeroElapsedTimeWhenJobIsNotRunning() {
    assertThat(job.isCurrentlyRunning()).isFalse();
    assertThat(job.elapsedTime()).isEqualTo("0s");
  }

  private PostView postView(Long id) {
    PostView postView = new PostView();
    postView.setId(id);
    postView.setNationalPostNumber("NWN/RM999/099/HT/009");
    postView.setOwner("North West");
    return postView;
  }

  private String postEsSql() {
    return "select * from Post "
        + "WHERECLAUSE "
        + "ORDERBYCLAUSE "
        + "LIMITCLAUSE";
  }
}
