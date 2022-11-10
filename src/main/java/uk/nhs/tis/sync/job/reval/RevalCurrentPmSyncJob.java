package uk.nhs.tis.sync.job.reval;

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
import uk.nhs.tis.sync.job.PersonCurrentPmSyncJobTemplate;
import uk.nhs.tis.sync.message.publisher.RabbitMqTcsPmUpdatePublisher;

/**
 * Get personIds whose current programmeMembership changes nightly. And sends messages to rabbitMq
 * for tcs to fetch
 */
@Profile("!nimdta")
@Component
@ManagedResource(objectName = "sync.mbean:name=RevalCurrentPmSyncJob",
    description = "Job message personIds if their programme membership(s) started/ended")
public class RevalCurrentPmSyncJob extends PersonCurrentPmSyncJobTemplate<Long> {

  private final RabbitMqTcsPmUpdatePublisher rabbitMqPublisher;

  public RevalCurrentPmSyncJob(RabbitMqTcsPmUpdatePublisher rabbitMqPublisher) {
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
}
