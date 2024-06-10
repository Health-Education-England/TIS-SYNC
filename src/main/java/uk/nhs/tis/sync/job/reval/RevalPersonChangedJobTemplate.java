package uk.nhs.tis.sync.job.reval;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import uk.nhs.tis.sync.job.PersonDateChangeCaptureSyncJobTemplate;
import uk.nhs.tis.sync.message.publisher.RabbitMqTcsRevalTraineeUpdatePublisher;
import uk.nhs.tis.sync.model.EntityData;

/**
 * This is a template abstract class for Revalidation Person job change.
 *
 * <p>Its purpose is to publish Persons based on their current Placement
 * into Rabbit message queue.
 */
public abstract class RevalPersonChangedJobTemplate extends
    PersonDateChangeCaptureSyncJobTemplate<Long> {

  private final RabbitMqTcsRevalTraineeUpdatePublisher rabbitMqPublisher;

  protected RevalPersonChangedJobTemplate(
      EntityManagerFactory entityManagerFactory,
      @Autowired(required = false) ApplicationEventPublisher applicationEventPublisher,
      RabbitMqTcsRevalTraineeUpdatePublisher rabbitMqPublisher) {
    super(entityManagerFactory, applicationEventPublisher);
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
