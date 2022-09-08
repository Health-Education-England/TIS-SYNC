package uk.nhs.tis.sync.job.person;

import com.google.common.base.Stopwatch;
import com.transformuk.hee.tis.tcs.service.job.person.PersonView;
import com.transformuk.hee.tis.tcs.service.service.helper.SqlQuerySupplier;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.index.IndexNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.event.JobExecutionEvent;
import uk.nhs.tis.sync.job.RunnableJob;
import uk.nhs.tis.sync.service.PersonElasticSearchService;
import uk.nhs.tis.sync.service.impl.PersonViewRowMapper;

@Component
@ManagedResource(objectName = "sync.mbean:name=PersonElasticSearchJob",
    description = "Service that clears the persons index in ES and repopulates the data")
public class PersonElasticSearchSyncJob implements RunnableJob {

  private static final String JOB_NAME = "Person sync job";
  private static final Logger LOG = LoggerFactory.getLogger(PersonElasticSearchSyncJob.class);
  private static final String ES_INDEX = "persons";
  private static final int FIFTEEN_MIN = 15 * 60 * 1000;

  private Stopwatch mainStopWatch;

  @Value("${application.jobs.personElasticSearchJob.pageSize:8000}")
  protected int pageSize = 8_000;

  @Autowired
  private SqlQuerySupplier sqlQuerySupplier;

  @Autowired
  private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  @Autowired
  private ElasticsearchOperations elasticSearchOperations;

  @Autowired
  private PersonElasticSearchService personElasticSearchService;

  @Autowired(required = false)
  private ApplicationEventPublisher applicationEventPublisher;

  @ManagedOperation(description = "Is the Person es sync just currently running")
  public boolean isCurrentlyRunning() {
    return mainStopWatch != null;
  }

  @ManagedOperation(description = "The current elapsed time of the current sync job")
  public String elapsedTime() {
    return mainStopWatch != null ? mainStopWatch.toString() : "0s";
  }

  protected void runSyncJob() {
    if (mainStopWatch != null) {
      LOG.info("Sync job [{}] already running, exiting this execution", JOB_NAME);
      return;
    }
    CompletableFuture.runAsync(this::run);
  }

  @Override
  public void run(@Nullable String params) {
    personElasticSearchSync();
  }

  @Scheduled(cron = "${application.cron.personElasticSearchJob}")
  @SchedulerLock(name = "personsElasticSearchScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(description = "Run sync of the persons es index")
  public void personElasticSearchSync() {
    runSyncJob();
  }

  private int getPageSize() {
    return pageSize;
  }

  private void deleteIndex() {
    LOG.info("deleting person es index");
    try {
      elasticSearchOperations.indexOps(IndexCoordinates.of(ES_INDEX)).delete();
    } catch (IndexNotFoundException e) {
      LOG.info("Could not delete an index that does not exist, continuing");
    }
  }

  private List<PersonView> collectData(int page, int pageSize) {
    String query = sqlQuerySupplier.getQuery(SqlQuerySupplier.PERSON_ES_VIEW);
    String limitClause = "limit " + pageSize + " offset " + page * pageSize;
    query = query.replace("TRUST_JOIN", "").replace("PROGRAMME_MEMBERSHIP_JOIN", "")
        .replace("WHERECLAUSE", "").replace("ORDERBYCLAUSE", "ORDER BY id DESC")
        .replace("LIMITCLAUSE", limitClause);

    MapSqlParameterSource paramSource = new MapSqlParameterSource();
    List<PersonView> queryResult =
        namedParameterJdbcTemplate.query(query, paramSource, new PersonViewRowMapper());
    personElasticSearchService.updateDocumentWithTrustData(queryResult);
    // this is to query from programmeMembership, Programme and TrainingNumber
    personElasticSearchService.updateDocumentWithProgrammeMembershipData(queryResult);
    return queryResult;
  }

  protected void run() {

    if (applicationEventPublisher != null) {
      applicationEventPublisher
          .publishEvent(new JobExecutionEvent(this, "Sync [" + JOB_NAME + "] started."));
    }
    try {
      LOG.info("Sync [{}] started", JOB_NAME);
      mainStopWatch = Stopwatch.createStarted();
      Stopwatch stopwatch = Stopwatch.createStarted();

      int totalRecords = 0;
      int page = 0;
      boolean hasMoreResults = true;

      deleteIndex();
      createIndex();
      stopwatch.reset().start();

      while (hasMoreResults) {

        List<PersonView> collectedData = collectData(page, getPageSize());
        page++;

        hasMoreResults = !collectedData.isEmpty();

        LOG.info("Time taken to read chunk : [{}]", stopwatch);
        if (CollectionUtils.isNotEmpty(collectedData)) {
          totalRecords += collectedData.size();
        }
        stopwatch.reset().start();

        personElasticSearchService.saveDocuments(collectedData);

        LOG.info("Time taken to save chunk : [{}]", stopwatch);
      }
      elasticSearchOperations.indexOps(PersonView.class).refresh();
      stopwatch.reset().start();
      LOG.info("Sync job [{}] finished. Total time taken {} for processing [{}] records",
          JOB_NAME, mainStopWatch.stop(), totalRecords);
      mainStopWatch = null;
      if (applicationEventPublisher != null) {
        applicationEventPublisher
            .publishEvent(new JobExecutionEvent(this, "Sync [" + JOB_NAME + "] finished."));
      }
    } catch (Exception e) {
      LOG.error(e.getLocalizedMessage(), e);
      mainStopWatch = null;
      if (applicationEventPublisher != null) {
        applicationEventPublisher.publishEvent(new JobExecutionEvent(this, "<!channel> Sync ["
            + JOB_NAME + "] failed with exception [" + e.getMessage() + "]."));
      }
    }
  }

  private void createIndex() {
    LOG.info("creating and updating mappings");
    elasticSearchOperations.indexOps(IndexCoordinates.of(ES_INDEX)).create();
    Document mapping = elasticSearchOperations.indexOps(IndexCoordinates.of(ES_INDEX))
        .createMapping(PersonView.class);
    elasticSearchOperations.indexOps(IndexCoordinates.of(ES_INDEX)).putMapping(mapping);
  }

}
