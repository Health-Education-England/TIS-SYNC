package uk.nhs.tis.sync.job;

import com.transformuk.hee.tis.tcs.service.repository.PostRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import javax.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.job.PersonDateChangeCaptureSyncJobTemplate;

@Component
public class PostFundingSyncJob extends PersonDateChangeCaptureSyncJobTemplate<Long> {


  @Autowired
  PostRepository postRepository;

  @Override
  public void run(String params) {
    postFundingSyncJob();
  }

  @Scheduled(cron = "${application.cron.postFundingSyncJob}")
  public void postFundingSyncJob() {
    super.runSyncJob(null);
  }

  @Override
  protected String buildQueryForDate(LocalDate dateOfChange) {
    String BASE_QUERY = "SELECT DISTINCT pf.postId FROM PostFunding pf " +
        "WHERE (pf.startDate = :dateOfChange OR pf.endDate = DATE_SUB(:dateOfChange, INTERVAL 1 DAY))";
    return BASE_QUERY;
  }

  @Override
  protected int convertData(Set<Long> entitiesToSave, List<Long> entityData,
      EntityManager entityManager) {
    entitiesToSave.addAll(entityData);
    return 0;
  }

  @Override
  protected void handleData(Set<Long> dataToSave, EntityManager entityManager) {
    LocalDate currentDate = LocalDate.now();
    String updateQuery = "UPDATE Post p " +
        "JOIN PostFunding pf ON p.id = pf.postId " +
        "SET p.status = " +
        "CASE " +
        "WHEN pf.startDate = ? THEN 'CURRENT' " +
        "WHEN pf.endDate = DATE_SUB(?, INTERVAL 1 DAY) THEN 'INACTIVE' " +
        "ELSE p.status " +
        "END";

    postRepository.save();
  }
}


