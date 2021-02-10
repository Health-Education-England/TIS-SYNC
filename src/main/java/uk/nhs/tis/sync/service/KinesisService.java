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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class KinesisService {

  private static final Logger LOG = LoggerFactory.getLogger(KinesisService.class);

  private static final String SCHEMA_TCS = "tcs";

  private static final String SCHEMA_REFERENCE = "reference";

  private AmazonKinesis amazonKinesis;

  private String kinesisStreamName;

  private ObjectMapper objectMapper;

  private DmsRecordAssembler dmsRecordAssembler;

  /**
   * An object to send data into a Kinesis data stream.
   * @param amazonKinesis Object needed to a PutRecordsRequest object into the stream.
   * @param kinesisStreamName Name of the Kinesis stream.
   */
  public KinesisService(
      AmazonKinesis amazonKinesis,
      DmsRecordAssembler dmsRecordAssembler,
      @Value("${application.aws.kinesis.streamName}") String kinesisStreamName) {
    this.amazonKinesis = amazonKinesis;
    this.dmsRecordAssembler = dmsRecordAssembler;
    this.kinesisStreamName = kinesisStreamName;
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
   * which is part of a list of entries, which can be set as records in a PutRecordRequest
   * object, which can be sent into a stream by the amazonKinesis object.
   * @param dto   The retrieved dto that is being sent into the stream.
   */
  public void sendData(Object dto) {
    PutRecordsRequest putRecordsRequest  = new PutRecordsRequest();

    putRecordsRequest.setStreamName(kinesisStreamName);

    List<PutRecordsRequestEntry> putRecordsRequestEntryList  = new ArrayList<>();

    PutRecordsRequestEntry putRecordsRequestEntry  = new PutRecordsRequestEntry();

    try {
      String jsonStringOutput = dmsRecordAssembler.buildRecord(dto);
      LOG.info("Trying to send {}", jsonStringOutput);

      putRecordsRequestEntry.setData(ByteBuffer.wrap(jsonStringOutput.getBytes()));
      int listSize = putRecordsRequestEntryList.size();
      putRecordsRequestEntry.setPartitionKey(String.format("partitionKey-%d", listSize));
      putRecordsRequestEntryList.add(putRecordsRequestEntry);

      putRecordsRequest.setRecords(putRecordsRequestEntryList);
      PutRecordsResult putRecordsResult  = amazonKinesis.putRecords(putRecordsRequest);

      LOG.info("Put Result {}", putRecordsResult);
    } catch (JsonProcessingException e) {
      LOG.info(e.getMessage());
    }
  }
}
