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

import com.transformuk.hee.tis.tcs.service.job.post.PostView;
import com.transformuk.hee.tis.tcs.service.service.helper.SqlQuerySupplier;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.job.AbstractElasticSearchSyncJob;
import uk.nhs.tis.sync.service.PostElasticSearchService;
import uk.nhs.tis.sync.service.impl.PostViewRowMapper;

/**
 * Job for rebuilding the posts Elasticsearch index from MySQL.
 *
 * <p>This job deletes the existing "posts" index, recreates the index and mapping from PostView,
 * reads post view data from MySQL in chunks, maps each row into PostView documents, saves those
 * documents into Elasticsearch, and refreshes the index after completion.</p>
 */
@Component
@ManagedResource(
    objectName = "sync.mbean:name=PostElasticSearchJob",
    description = "Service that clears the posts index in ES and repopulates the data"
)
@Slf4j
public class PostElasticSearchSyncJob extends AbstractElasticSearchSyncJob<PostView> {

  private static final String JOB_NAME = "Post Elasticsearch sync job";
  private static final String ES_INDEX = "posts";
  private static final int FIFTEEN_MIN = 15 * 60 * 1000;

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  private final SqlQuerySupplier sqlQuerySupplier;
  private final PostElasticSearchService postElasticSearchService;
  private final PostViewRowMapper postViewRowMapper;

  /**
   * Elasticsearch sync job for Post list searching.
   */
  public PostElasticSearchSyncJob(
      NamedParameterJdbcTemplate namedParameterJdbcTemplate,
      SqlQuerySupplier sqlQuerySupplier,
      ElasticsearchOperations elasticSearchOperations,
      PostElasticSearchService postElasticSearchService,
      PostViewRowMapper postViewRowMapper,
      ApplicationEventPublisher applicationEventPublisher,
      @Value("${application.jobs.postElasticSearchJob.pageSize:8000}") int pageSize) {

    super(elasticSearchOperations, applicationEventPublisher, pageSize);
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    this.sqlQuerySupplier = sqlQuerySupplier;
    this.postElasticSearchService = postElasticSearchService;
    this.postViewRowMapper = postViewRowMapper;
  }

  @Scheduled(cron = "${application.cron.postElasticSearchJob}")
  @SchedulerLock(
      name = "postsElasticSearchScheduledTask",
      lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN
  )
  @ManagedOperation(description = "Run sync of the posts Elasticsearch index")
  public void postElasticSearchSync() {
    runSyncJob();
  }

  void runSynchronously() {
    super.run();
  }

  @Override
  protected String getJobName() {
    return JOB_NAME;
  }

  @Override
  protected String getIndexName() {
    return ES_INDEX;
  }

  @Override
  protected Class<PostView> getDocumentClass() {
    return PostView.class;
  }

  @Override
  protected List<PostView> collectData(int page, int pageSize) {
    log.info("Collecting post elastic search data. page={}, pageSize={}", page, pageSize);
    String query = sqlQuerySupplier.getQuery(SqlQuerySupplier.POST_VIEW);

    String limitClause = "limit " + pageSize + " offset " + page * pageSize;

    query = query
        .replace("WHERECLAUSE", "")
        .replace("ORDERBYCLAUSE", "ORDER BY id DESC")
        .replace("LIMITCLAUSE", limitClause);

    log.info("Running post ES SQL for page={}", page);
    log.debug("Post ES SQL: {}", query);
    MapSqlParameterSource paramSource = new MapSqlParameterSource();
    List<PostView> result = namedParameterJdbcTemplate.query(query, paramSource, postViewRowMapper);
    log.info("Collected {} Post ES records for page={}", result.size(), page);

    return result;
  }

  @Override
  protected void saveDocuments(List<PostView> documents) {
    postElasticSearchService.saveDocuments(documents);
  }
}
