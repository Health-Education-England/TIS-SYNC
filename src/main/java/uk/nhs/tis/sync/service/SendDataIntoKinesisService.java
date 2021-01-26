package uk.nhs.tis.sync.service;

import java.util.ArrayList;
import java.util.List;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.ByteBuffer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.nhs.tis.sync.dto.InputDto;
import uk.nhs.tis.sync.dto.MetadataDto;

@Service
public class SendDataIntoKinesisService {

  private static final Logger LOG = LoggerFactory.getLogger(SendDataIntoKinesisService.class);

  private AmazonKinesis amazonKinesis;

  private String kinesisStreamName;

  private ObjectMapper objectMapper;

  /**
   * An object to send data into a Kinesis data stream.
   * @param amazonKinesis Object needed to a PutRecordsRequest object into the stream.
   * @param kinesisStreamName Name of the Kinesis stream.
   */
  public SendDataIntoKinesisService(
      AmazonKinesis amazonKinesis,
      @Value("${application.aws.kinesis.streamName}") String kinesisStreamName) {
    this.amazonKinesis = amazonKinesis;
    this.kinesisStreamName = kinesisStreamName;
    this.objectMapper = new ObjectMapper();
  }

  public void sendDataIntoKinesisStream(Object dto) {
    PutRecordsRequest putRecordsRequest  = new PutRecordsRequest();
    putRecordsRequest.setStreamName(kinesisStreamName);
    List<PutRecordsRequestEntry> putRecordsRequestEntryList  = new ArrayList<>();

    PutRecordsRequestEntry putRecordsRequestEntry  = new PutRecordsRequestEntry();

    String jsonStringInput = buildDataInput(dto);
    putRecordsRequestEntry.setData(ByteBuffer.wrap(jsonStringInput.getBytes()));
    int listSize = putRecordsRequestEntryList.size();
    putRecordsRequestEntry.setPartitionKey(String.format("partitionKey-%d", listSize));
    putRecordsRequestEntryList.add(putRecordsRequestEntry);

    putRecordsRequest.setRecords(putRecordsRequestEntryList);
    PutRecordsResult putRecordsResult  = amazonKinesis.putRecords(putRecordsRequest);
    LOG.info("Put Result {}", putRecordsResult);
  }

  private String buildDataInput(Object dto) {
    MetadataDto metadataDto = new MetadataDto("tcs", "Post", "load");
    InputDto inputDto = new InputDto(dto, metadataDto);
    String json = null;
    try {
      json = objectMapper.writeValueAsString(inputDto);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return json;
  }
}
