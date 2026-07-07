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

package uk.nhs.tis.sync.job;

import com.google.common.base.Stopwatch;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.elasticsearch.index.IndexNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.jmx.export.annotation.ManagedOperation;
import uk.nhs.tis.sync.event.JobExecutionEvent;

/**
 * A common Class for elastic search sync jobs.
 */
@Slf4j
public abstract class AbstractElasticSearchSyncJob<T> implements RunnableJob {

  private final ElasticsearchOperations elasticSearchOperations;
  private final int pageSize;
  private final ApplicationEventPublisher applicationEventPublisher;
  private Stopwatch mainStopWatch;

  protected AbstractElasticSearchSyncJob(
      ElasticsearchOperations elasticSearchOperations,
      ApplicationEventPublisher applicationEventPublisher,
      int pageSize) {
    this.elasticSearchOperations = elasticSearchOperations;
    this.applicationEventPublisher = applicationEventPublisher;
    this.pageSize = pageSize;
  }

  protected void runSyncJob() {
    if (mainStopWatch != null) {
      log.info("{} is already running.", getJobName());
      return;
    }

    CompletableFuture.runAsync(this::run);
  }

  protected void run() {
    publishEvent("Sync service [" + getJobName() + "] started.");

    try {
      log.info("{} started.", getJobName());

      mainStopWatch = Stopwatch.createStarted();
      Stopwatch stopwatch = Stopwatch.createStarted();

      int page = 0;
      int totalRecords = 0;
      boolean hasMoreResults = true;

      deleteIndex();
      createIndex();

      while (hasMoreResults) {
        stopwatch.reset().start();

        List<T> collectedData = collectData(page, pageSize);

        log.info("Time taken to read {} chunk page {}: {}",
            getIndexName(), page, stopwatch);

        page++;
        hasMoreResults = CollectionUtils.isNotEmpty(collectedData);

        if (CollectionUtils.isNotEmpty(collectedData)) {
          totalRecords += collectedData.size();

          stopwatch.reset().start();
          saveDocuments(collectedData);

          log.info("Time taken to save {} chunk: {}", getIndexName(), stopwatch);
        }
      }

      refreshIndex();

      log.info("{} finished. Total time taken {} for processing {} records.",
          getJobName(), mainStopWatch.stop(), totalRecords);

      publishEvent("Synch [" + getJobName() + "] finished.");
    } catch (Exception e) {
      log.error("{} failed.", getJobName(), e);

      publishEvent("Data Sync [" + getJobName() + "] failed with exception ["
          + e.getMessage() + "].");
    } finally {
      mainStopWatch = null;
    }
  }

  @Override
  public void run(String params) {
    runSyncJob();
  }

  @ManagedOperation(description = "Is the Elasticsearch sync currently running")
  public boolean isCurrentlyRunning() {
    return mainStopWatch != null;
  }

  @ManagedOperation(description = "The current elapsed time of the Elasticsearch sync job")
  public String elapsedTime() {
    return mainStopWatch != null ? mainStopWatch.toString() : "0s";
  }

  private void deleteIndex() {
    try {
      elasticSearchOperations.indexOps(IndexCoordinates.of(getIndexName())).delete();
    } catch (IndexNotFoundException e) {
      log.info("Index {} does not exist, continuing.", getIndexName());
    }
  }

  private void createIndex() {
    elasticSearchOperations.indexOps(IndexCoordinates.of(getIndexName())).create();

    Document mapping = elasticSearchOperations
        .indexOps(IndexCoordinates.of(getIndexName()))
        .createMapping(getDocumentClass());

    elasticSearchOperations
        .indexOps(IndexCoordinates.of(getIndexName()))
        .putMapping(mapping);
  }

  private void refreshIndex() {
    elasticSearchOperations.indexOps(getDocumentClass()).refresh();
  }

  private void publishEvent(String message) {
    applicationEventPublisher.publishEvent(new JobExecutionEvent(this, message));
  }

  protected abstract String getJobName();

  protected abstract String getIndexName();

  protected abstract Class<T> getDocumentClass();

  protected abstract List<T> collectData(int page, int pageSize);

  protected abstract void saveDocuments(List<T> documents);
}
