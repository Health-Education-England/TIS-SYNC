package uk.nhs.tis.sync.job.person;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.transformuk.hee.tis.tcs.service.job.person.PersonView;
import com.transformuk.hee.tis.tcs.service.service.helper.SqlQuerySupplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import uk.nhs.tis.sync.event.JobExecutionEvent;
import uk.nhs.tis.sync.service.PersonElasticSearchService;

@ExtendWith(MockitoExtension.class)
class PersonElasticSearchSyncJobTest {

  @Mock
  NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  @Mock
  SqlQuerySupplier sqlQuerySupplier;
  @Mock
  ElasticsearchOperations elasticSearchOperations;
  @Mock
  PersonElasticSearchService personElasticSearchService;
  @Mock
  private ApplicationEventPublisher applicationEventPublisher;
  @Mock
  private IndexOperations mockIndexOps;
  @Captor
  private ArgumentCaptor<JobExecutionEvent> appEventCaptor;
  int pageSize = 42;

  private PersonElasticSearchSyncJob job;

  @BeforeEach
  void setupWithMocks() {
    job = new PersonElasticSearchSyncJob(namedParameterJdbcTemplate, sqlQuerySupplier,
        elasticSearchOperations, personElasticSearchService, pageSize);
    job.setApplicationEventPublisher(applicationEventPublisher);

    when(elasticSearchOperations.indexOps(any(IndexCoordinates.class)))
        .thenReturn(mockIndexOps);
    when(mockIndexOps.createMapping(PersonView.class)).thenReturn(Document.create());
    //TODO Finish mocks for a whole
  }

  @Test
  void testRunNormallyCompletes() {
    job.run("foo");
    verify(applicationEventPublisher).publishEvent(appEventCaptor.capture());
    //TODO Verify capture of "completed" event as the last of several (2?) app events
  }
}