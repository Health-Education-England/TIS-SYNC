package uk.nhs.tis.sync.service;

import java.util.ArrayList;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.nio.ByteBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Service;
import uk.nhs.tis.sync.dto.OutputDto;
import uk.nhs.tis.sync.dto.MetadataDto;

@Service
@EnableWebSecurity(debug = false)
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

    String jsonStringOutput = buildDataOutput(dto);

    if (jsonStringOutput != null) {
      putRecordsRequestEntry.setData(ByteBuffer.wrap(jsonStringOutput.getBytes()));
    }

    int listSize = putRecordsRequestEntryList.size();
    putRecordsRequestEntry.setPartitionKey(String.format("partitionKey-%d", listSize));
    putRecordsRequestEntryList.add(putRecordsRequestEntry);

    putRecordsRequest.setRecords(putRecordsRequestEntryList);
    PutRecordsResult putRecordsResult  = amazonKinesis.putRecords(putRecordsRequest);
    LOG.info("Put Result {}", putRecordsResult);
  }

  private String buildDataOutput(Object dto) {
    MetadataDto metadataDto = new MetadataDto("tcs", "Post", "load");
    OutputDto outputDto = new OutputDto(dto, metadataDto);
    String json = null;
    try {
      json = objectMapper.writeValueAsString(outputDto);
    } catch (JsonProcessingException e) {
      LOG.info(e.getMessage());
    }
    return json;
  }
}
