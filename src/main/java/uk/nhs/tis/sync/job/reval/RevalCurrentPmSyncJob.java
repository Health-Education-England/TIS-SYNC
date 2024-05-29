package uk.nhs.tis.sync.job.reval;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.message.publisher.RabbitMqTcsRevalTraineeUpdatePublisher;

/**
 * Get personIds whose current programmeMembership changes nightly. And sends messages to rabbitMq
 * for tcs to fetch
 */
@Profile("!nimdta")
@Component
@ManagedResource(objectName = "sync.mbean:name=RevalCurrentPmSyncJob",
    description = "Job message personIds if their programme membership(s) started/ended")
public class RevalCurrentPmSyncJob extends RevalPersonChangedJobTemplate {

  private static final String BASE_QUERY =
      "SELECT DISTINCT personId FROM ProgrammeMembership" + " WHERE personId > :lastPersonId"
          + " AND (programmeEndDate = ':endDate' OR programmeStartDate = ':startDate')"
          + " ORDER BY personId LIMIT :pageSize";

  public RevalCurrentPmSyncJob(RabbitMqTcsRevalTraineeUpdatePublisher rabbitMqPublisher) {
    super(rabbitMqPublisher);
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
  protected String buildQueryForDate(LocalDate dateOfChange) {
    if (dateOfChange != null) {
      String startDate = dateOfChange.format(DateTimeFormatter.ISO_LOCAL_DATE);
      String endDate = dateOfChange.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
      return BASE_QUERY.replace(":endDate", endDate).replace(":startDate", startDate)
          .replace(":pageSize", "" + getPageSize());
    } else {
      return BASE_QUERY.replace(":pageSize", "" + getPageSize())
          .replace(" AND (programmeEndDate = ':endDate' OR programmeStartDate = ':startDate')", "");
    }
  }
}
