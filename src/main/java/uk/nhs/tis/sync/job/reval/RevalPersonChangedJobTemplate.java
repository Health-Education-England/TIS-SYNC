package uk.nhs.tis.sync.job.reval;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import org.apache.commons.collections4.CollectionUtils;
import uk.nhs.tis.sync.job.PersonDateChangeCaptureSyncJobTemplate;
import uk.nhs.tis.sync.message.publisher.RabbitMqTcsRevalTraineeUpdatePublisher;
import uk.nhs.tis.sync.model.EntityData;

public abstract class RevalPersonChangedJobTemplate extends
    PersonDateChangeCaptureSyncJobTemplate<Long> {

  private final RabbitMqTcsRevalTraineeUpdatePublisher rabbitMqPublisher;

  protected RevalPersonChangedJobTemplate(
      RabbitMqTcsRevalTraineeUpdatePublisher rabbitMqPublisher) {
    this.rabbitMqPublisher = rabbitMqPublisher;
  }

  @Override
  protected int convertData(Set<Long> entitiesToSave, List<EntityData> entityData,
      EntityManager entityManager) {
    entitiesToSave.addAll(
        entityData.stream().map(EntityData::getEntityId).collect(Collectors.toList()));
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
