package uk.nhs.tis.sync.job.person;

import com.google.common.base.Stopwatch;
import com.transformuk.hee.tis.tcs.service.job.person.PersonView;
import com.transformuk.hee.tis.tcs.service.service.helper.SqlQuerySupplier;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.index.IndexNotFoundException;
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

/**
 * This job runs on a daily basis and provides support for faster data search
 * <p>
 * Its purpose is to clear down the persons index in ES then repopulates the person data so that
 * person search is quicker.
 */
@Component
@ManagedResource(objectName = "sync.mbean:name=PersonElasticSearchJob",
    description = "Service that clears the persons index in ES and repopulates the data")
@Slf4j
public class PersonElasticSearchSyncJob implements RunnableJob {

  private static final String JOB_NAME = "Person sync job";
  private static final String ES_INDEX = "persons";
  private static final int FIFTEEN_MIN = 15 * 60 * 1000;

  private final SqlQuerySupplier sqlQuerySupplier;

  private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

  private final ElasticsearchOperations elasticSearchOperations;

  private final PersonElasticSearchService personElasticSearchService;

  private ApplicationEventPublisher applicationEventPublisher;

  protected int pageSize = 8_000;
  private Stopwatch mainStopWatch;


  @Autowired
  public PersonElasticSearchSyncJob(NamedParameterJdbcTemplate namedParameterJdbcTemplate,
      SqlQuerySupplier sqlQuerySupplier, ElasticsearchOperations elasticSearchOperations,
      PersonElasticSearchService personElasticSearchService,
      @Value("${application.jobs.personElasticSearchJob.pageSize:8000}") int pageSize) {
    this.sqlQuerySupplier = sqlQuerySupplier;
    this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    this.elasticSearchOperations = elasticSearchOperations;
    this.personElasticSearchService = personElasticSearchService;
    this.pageSize = pageSize;
  }

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
      log.info("Sync job [{}] already running, exiting this execution", JOB_NAME);
      return;
    }
    CompletableFuture.runAsync(this::run);
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
    log.info("deleting person es index");
    try {
      elasticSearchOperations.indexOps(IndexCoordinates.of(ES_INDEX)).delete();
    } catch (IndexNotFoundException e) {
      log.info("Could not delete an index that does not exist, continuing");
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

  @Override
  public void run(@Nullable String params) {
    personElasticSearchSync();
  }

  protected void run() {
    if (applicationEventPublisher != null) {
      applicationEventPublisher
          .publishEvent(new JobExecutionEvent(this, "Sync [" + JOB_NAME + "] started."));
    }
    try {
      log.info("Sync [{}] started", JOB_NAME);
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

        log.info("Time taken to read chunk : [{}]", stopwatch);
        if (CollectionUtils.isNotEmpty(collectedData)) {
          totalRecords += collectedData.size();
        }
        stopwatch.reset().start();

        personElasticSearchService.saveDocuments(collectedData);

        log.info("Time taken to save chunk : [{}]", stopwatch);
      }
      elasticSearchOperations.indexOps(PersonView.class).refresh();
      stopwatch.reset().start();
      log.info("Sync job [{}] finished. Total time taken {} for processing [{}] records",
          JOB_NAME, mainStopWatch.stop(), totalRecords);
      mainStopWatch = null;
      if (applicationEventPublisher != null) {
        applicationEventPublisher
            .publishEvent(new JobExecutionEvent(this, "Sync [" + JOB_NAME + "] finished."));
      }
    } catch (Exception e) {
      log.error(e.getLocalizedMessage(), e);
      mainStopWatch = null;
      if (applicationEventPublisher != null) {
        applicationEventPublisher.publishEvent(new JobExecutionEvent(this,
            "<!channel> Sync [" + JOB_NAME + "] failed with exception [" + e.getMessage() + "]."));
      }
    }
  }

  private void createIndex() {
    log.info("creating and updating mappings");
    elasticSearchOperations.indexOps(IndexCoordinates.of(ES_INDEX)).create();
    Document mapping = elasticSearchOperations.indexOps(IndexCoordinates.of(ES_INDEX))
        .createMapping(PersonView.class);
    elasticSearchOperations.indexOps(IndexCoordinates.of(ES_INDEX)).putMapping(mapping);
  }

  @Autowired(required = false)
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }
}
