package uk.nhs.tis.sync.job.reval;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.job.PersonDateChangeCaptureSyncJobTemplate;
import uk.nhs.tis.sync.message.publisher.RabbitMqTcsRevalTraineeUpdatePublisher;

/**
 * Get personIds whose current programmeMembership changes nightly. And sends messages to rabbitMq
 * for tcs to fetch
 */
@Profile("!nimdta")
@Component
@ManagedResource(objectName = "sync.mbean:name=RevalCurrentPmSyncJob",
    description = "Job message personIds if their programme membership(s) started/ended")
public class RevalCurrentPmSyncJob extends PersonDateChangeCaptureSyncJobTemplate<Long> {

  private static final int DEFAULT_PAGE_SIZE = 5000;
  private static final int FIFTEEN_MIN = 15 * 60 * 1000;
  private static final String BASE_QUERY =
      "SELECT DISTINCT personId FROM ProgrammeMembership" + " WHERE personId > :lastPersonId"
          + " AND (programmeEndDate = ':endDate' OR programmeStartDate = ':startDate')"
          + " ORDER BY personId LIMIT :pageSize";
  private final RabbitMqTcsRevalTraineeUpdatePublisher rabbitMqPublisher;

  public RevalCurrentPmSyncJob(RabbitMqTcsRevalTraineeUpdatePublisher rabbitMqPublisher) {
    this.rabbitMqPublisher = rabbitMqPublisher;
  }

  @Override
  public void run(String params) {
    revalCurrentPmSyncJob();
  }

  @Profile("!nimdta")
  @Scheduled(cron = "${application.cron.revalCurrentPmJob}")
  @SchedulerLock(name = "revalCurrentPmScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(
      description = "send personIds to tcs for reval current programmeMembership sync")
  public void revalCurrentPmSyncJob() {
    runSyncJob(null);
  }

  @Override
  protected int convertData(Set<Long> entitiesToSave, List<Long> entityData,
      EntityManager entityManager) {
    entitiesToSave.addAll(entityData);
    return 0;
  }

  @Override
  protected void handleData(Set<Long> dataToSave, EntityManager entityManager) {
    if (CollectionUtils.isNotEmpty(dataToSave)) {
      rabbitMqPublisher.publishToBroker(
          dataToSave.stream().map(String::valueOf).collect(Collectors.toSet()));
    }
  }

  @Override
  protected String buildQueryForDate(LocalDate dateOfChange) {
    if (dateOfChange != null) {
      String startDate = dateOfChange.format(DateTimeFormatter.ISO_LOCAL_DATE);
      String endDate = dateOfChange.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
      return BASE_QUERY.replace(":endDate", endDate).replace(":startDate", startDate)
          .replace(":pageSize", "" + DEFAULT_PAGE_SIZE);
    } else {
      return BASE_QUERY.replace(":pageSize", "" + DEFAULT_PAGE_SIZE)
          .replace(" AND (programmeEndDate = ':endDate' OR programmeStartDate = ':startDate')", "");
    }
  }

}
