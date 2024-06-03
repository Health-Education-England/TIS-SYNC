package uk.nhs.tis.sync.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.service.model.Person;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.model.EntityData;

/**
 * A sync job for updating training record status for a person.
 *
 * This job sets a Person's (Training Record) Status if their
 * programme membership(s) started/ended.
 *
 */
@Component
@ManagedResource(objectName = "sync.mbean:name=PersonRecordStatusJob",
    description = "Job set a Person's (Training Record) Status if their "
        + "programme membership(s) started/ended")
@Slf4j
public class PersonRecordStatusJob extends PersonDateChangeCaptureSyncJobTemplate<Person> {

  private static final String BASE_QUERY =
      "SELECT DISTINCT personId FROM ProgrammeMembership" + " WHERE personId > :lastPersonId"
          + " AND (programmeEndDate = ':endDate' OR programmeStartDate = ':startDate')"
          + " ORDER BY personId LIMIT :pageSize";

  private final ObjectMapper objectMapper;

  public PersonRecordStatusJob(EntityManagerFactory entityManagerFactory,
      @Autowired(required = false) ApplicationEventPublisher applicationEventPublisher,
      ObjectMapper objectMapper) {
    super(entityManagerFactory, applicationEventPublisher);
    this.objectMapper = objectMapper;
  }

  @Value("${application.jobs.personRecordStatusJob.dateOfChangeOverride}")
  public void setDateOverride(String dateOfChangeOverride) {
    this.dateOfChangeOverride = dateOfChangeOverride;
  }

  public void run(String params) {
    personRecordStatusJob(params);
  }

  @Scheduled(cron = "${application.cron.personRecordStatusJob}")
  @SchedulerLock(name = "personRecordStatusScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
      lockAtMostFor = FIFTEEN_MIN)
  @ManagedOperation(
      description = "Run sync of the ProgrammeMembership table to update Person status")
  public void personRecordStatusJob() {
    runSyncJob(null);
  }

  /**
   * Trigger the personRecordStatusJob with the specified date.
   *
   * @param jsonParams The only recognised property is `dateOverride`.  The value can be "ANY",
   *                   "NONE", empty or a date in format yyyy-MM-dd
   */
  public void personRecordStatusJob(String jsonParams) {
    log.debug("Received run params [{}]", jsonParams);
    String date = null;
    if (StringUtils.isNotEmpty(jsonParams)) {
      try {
        date = objectMapper.readTree(jsonParams).get("dateOverride").textValue();
        validateDateParamFormat(date);
        log.debug("Got validated date [{}]", date);
      } catch (JsonProcessingException e) {
        String errorMsg = "Unable to extract the dateOverride property";
        log.error(errorMsg, e);
        throw new IllegalArgumentException(errorMsg);
      } catch (DateTimeParseException e) {
        String errorMsg = String.format("The date is not correct: %s", date);
        log.error(errorMsg, e);
        throw new IllegalArgumentException(errorMsg);
      }
    }
    runSyncJob(date);
  }

  private LocalDate validateDateParamFormat(String dateStr) throws DateTimeParseException {
    if (StringUtils.isEmpty(dateStr) || StringUtils.equalsIgnoreCase(dateStr, FULL_SYNC_DATE_STR)
        || StringUtils.equalsIgnoreCase(dateStr, NO_DATE_OVERRIDE)) {
      return null;
    }
    // if the date format is incorrect, throw a DateTimeParseException
    return LocalDate.parse(dateStr);
  }

  @Override
  protected int convertData(Set<Person> entitiesToSave, List<EntityData> entityData,
                            EntityManager entityManager) {
    int entities = entityData.size();
    entityData.stream().map(entity -> entityManager.find(Person.class, entity.getEntityId()))
        .filter(Objects::nonNull)
        .filter(p -> p.getStatus() != p.programmeMembershipsStatus())
        .forEach(p -> {
          p.setStatus(p.programmeMembershipsStatus());
          entitiesToSave.add(p);
        });
    return entities - entitiesToSave.size();
  }

  @Override
  protected void handleData(Set<Person> dataToSave, EntityManager entityManager) {
    if (CollectionUtils.isNotEmpty(dataToSave)) {
      dataToSave.forEach(entityManager::persist);
      entityManager.flush();
    }
  }

  @Override
  protected String buildQueryForDate(LocalDate dateOfChange) {
    if (dateOfChange != null) {
      String startDate = dateOfChange.format(DateTimeFormatter.ISO_LOCAL_DATE);
      String endDate = dateOfChange.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
      return BASE_QUERY.replace(":endDate", endDate).replace(":startDate", startDate)
          .replace(":pageSize", "" + this.getPageSize());
    } else {
      return BASE_QUERY.replace(":pageSize", "" + this.getPageSize())
          .replace(" AND (programmeEndDate = ':endDate' OR programmeStartDate = ':startDate')", "");
    }
  }
}
