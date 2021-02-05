package uk.nhs.tis.sync.service;

import com.transformuk.hee.tis.reference.client.impl.ReferenceServiceImpl;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import uk.nhs.tis.sync.dto.AmazonSqsMessageDto;

@Slf4j
@Service
public class DataRequestService {

  public static final String TABLE_POST = "Post";
  public static final String TABLE_TRUST = "Trust";

  private static final Logger LOG = LoggerFactory.getLogger(DataRequestService.class);

  private TcsServiceImpl tcsServiceImpl;

  private ReferenceServiceImpl referenceServiceImpl;

  public DataRequestService(TcsServiceImpl tcsServiceImpl, ReferenceServiceImpl referenceServiceImpl) {
    this.tcsServiceImpl = tcsServiceImpl;
    this.referenceServiceImpl = referenceServiceImpl;
  }

  /**
   * Retrieve a DTO using TcsServiceImpl according to the info contained in an Amazon SQS message.
   * @param amazonSqsMessageDto The amazonSqsMessageDto to get info from for DTO retrieval.
   */
  public Object retrieveDto(AmazonSqsMessageDto amazonSqsMessageDto) {
    String table = amazonSqsMessageDto.getTable();
    String id = amazonSqsMessageDto.getId();

    Object dto = null;

    if (table.equals(TABLE_POST)) {
      try {
        dto = tcsServiceImpl.getPostById(Long.parseLong(id));
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }

    if (table.equals(TABLE_TRUST)) {
      try {
        dto = referenceServiceImpl.findTrustById(Long.parseLong(id));
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      }
    }

    return dto;
  }
}
