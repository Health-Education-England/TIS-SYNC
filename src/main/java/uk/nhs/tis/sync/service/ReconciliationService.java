package uk.nhs.tis.sync.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.nhs.tis.sync.dto.AmazonSQSMessageDto;
import org.slf4j.Logger;
import uk.nhs.tis.sync.job.SyncHandlingJob;

@Slf4j
@Service
public class ReconciliationService {

  private String TABLE_POST = "Post";

  private static final Logger LOG = LoggerFactory.getLogger(ReconciliationService.class);

  private TcsServiceImpl tcsServiceImpl;

  public ReconciliationService(TcsServiceImpl tcsServiceImpl) {
    this.tcsServiceImpl = tcsServiceImpl;
  }

  /**
   * Retrieve a DTO using TcsServiceImpl according to the info contained in an Amazon SQS message.
   *
   * @param amazonSQSMessageDto The amazonSQSMessageDto to get info from for DTO retrieval.
   */
  public Object retrieveDTO(AmazonSQSMessageDto amazonSQSMessageDto) {
    String table = amazonSQSMessageDto.getTable();
    String id = amazonSQSMessageDto.getId();

    Object dto = null;

    if (table.equals("Post")) {
      try {
        dto = (PostDTO) tcsServiceImpl.getPostById(Long.parseLong(id));
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }

    return dto;
  }
}
