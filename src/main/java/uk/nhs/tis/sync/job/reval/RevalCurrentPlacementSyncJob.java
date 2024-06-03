package uk.nhs.tis.sync.job.reval;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.persistence.EntityManagerFactory;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Profile;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.message.publisher.RabbitMqTcsRevalTraineeUpdatePublisher;

/**
 * Get personIds whose current placement changed overnight, i.e. ended yesterday or started today.
 * This sends messages for further processing, currently tcs to fetch and forward relevant data.
 */
@Profile("!nimdta")
@Component
@ManagedResource(objectName = "sync.mbean:name=RevalCurrentPlacementSyncJob",
    description = "Job message personIds if their placement(s) started/ended")
public class RevalCurrentPlacementSyncJob extends RevalPersonChangedJobTemplate {

  private static final String BASE_QUERY =
      "SELECT DISTINCT traineeId FROM Placement" + " WHERE traineeId > :lastPersonId"
          + " AND (dateFrom = ':today' OR dateTo = ':yesterday')"
          + " ORDER BY traineeId LIMIT :pageSize";

  public RevalCurrentPlacementSyncJob(EntityManagerFactory entityManagerFactory,
      @Autowired(required = false) ApplicationEventPublisher applicationEventPublisher,
      RabbitMqTcsRevalTraineeUpdatePublisher rabbitMqPublisher) {
    super(entityManagerFactory, applicationEventPublisher, rabbitMqPublisher);
  }

  @Override
  public void run(String params) {
    revalCurrentPlacementSyncJob();
  }

  @Profile("!nimdta")
  @Scheduled(cron = "${application.cron.revalCurrentPlacementJob}")
  @SchedulerLock(name = "revalCurrentPlacementScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(
      description = "send personIds to tcs for reval current placement sync")
  public void revalCurrentPlacementSyncJob() {
    runSyncJob(null);
  }

  @Override
  protected String buildQueryForDate(LocalDate dateOfChange) {
    String today = dateOfChange.format(DateTimeFormatter.ISO_LOCAL_DATE);
    String yesterday = dateOfChange.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
    return BASE_QUERY.replace(":yesterday", yesterday).replace(":today", today)
        .replace(":pageSize", "" + getPageSize());
  }
}
