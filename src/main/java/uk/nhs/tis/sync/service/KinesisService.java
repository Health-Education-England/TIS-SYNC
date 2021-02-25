package uk.nhs.tis.sync.service;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.nhs.tis.sync.dto.DmsDto;

@Service
public class KinesisService {

  public static final String PARTITION_KEY = "0";
  private static final Logger LOG = LoggerFactory.getLogger(KinesisService.class);
  private final AmazonKinesis amazonKinesis;
  private ObjectMapper objectMapper;

  /**
   * An object to send data into a Kinesis data stream.
   *
   * @param amazonKinesis Object needed to a PutRecordsRequest object into the stream.
   */
  public KinesisService(
      AmazonKinesis amazonKinesis) {
    this.amazonKinesis = amazonKinesis;
    this.objectMapper = new ObjectMapper();
  }

  public ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  public void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * To send data into a stream it's necessary to have data as a string (in json format in this
   * case), wrapped as bytes in a ByteBuffer, and set as data in a PutRecordRequestEntry object,
   * which is part of a list of entries, which can be set as records in a PutRecordRequest object,
   * which can be sent into a stream by the amazonKinesis object.
   *
   * @param kinesisStreamName The name of the stream where shards are being sent into.
   * @param dmsDtoList        A list of DmsDtos ready to be transformed into json strings.
   */
  public void sendData(String kinesisStreamName, List<DmsDto> dmsDtoList) {
    PutRecordsRequest putRecordsRequest = new PutRecordsRequest();

    putRecordsRequest.setStreamName(kinesisStreamName);

    List<PutRecordsRequestEntry> putRecordsRequestEntryList = new ArrayList<>();

    try {
      for (DmsDto dmsDto : dmsDtoList) {
        String jsonString = objectMapper.writeValueAsString(dmsDto);
        LOG.info("Trying to send{}", jsonString);
        PutRecordsRequestEntry putRecordsRequestEntry = new PutRecordsRequestEntry();
        putRecordsRequestEntry.setData(ByteBuffer.wrap(jsonString.getBytes()));
        putRecordsRequestEntryList.add(putRecordsRequestEntry);
        putRecordsRequestEntry.setPartitionKey(PARTITION_KEY);
      }

      putRecordsRequest.setRecords(putRecordsRequestEntryList);
      PutRecordsResult putRecordsResult = amazonKinesis.putRecords(putRecordsRequest);

      LOG.info("Put Result {}", putRecordsResult);
    } catch (JsonProcessingException e) {
      LOG.info(e.getMessage());
    }
  }
}
