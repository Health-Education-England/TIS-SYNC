package uk.nhs.tis.sync.service;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Service
public class SendDataIntoKinesisService {

  private static final Logger LOG = LoggerFactory.getLogger(SendDataIntoKinesisService.class);

  private AmazonKinesis amazonKinesis;

  private String kinesisStreamName;

  public SendDataIntoKinesisService(AmazonKinesis amazonKinesis,
                                    @Value("${application.aws.kinesis.streamName}") String kinesisStreamName) {
    this.amazonKinesis = amazonKinesis;
    this.kinesisStreamName = kinesisStreamName;
  }

  public void sendDataIntoKinesisStream(Object dto) {
    PutRecordsRequest putRecordsRequest  = new PutRecordsRequest();
    putRecordsRequest.setStreamName(kinesisStreamName);
    List<PutRecordsRequestEntry> putRecordsRequestEntryList  = new ArrayList<>();

    PutRecordsRequestEntry putRecordsRequestEntry  = new PutRecordsRequestEntry();
    putRecordsRequestEntry.setData(ByteBuffer.wrap(stringifyDto(dto).getBytes()));
    putRecordsRequestEntry.setPartitionKey(String.format("partitionKey-%d", putRecordsRequestEntryList.size()));
    putRecordsRequestEntryList.add(putRecordsRequestEntry);

    putRecordsRequest.setRecords(putRecordsRequestEntryList);
    PutRecordsResult putRecordsResult  = amazonKinesis.putRecords(putRecordsRequest);
    LOG.info("Put Result" + putRecordsResult);
  }


  private String stringifyDto(Object dto) {
    ObjectMapper mapper = new ObjectMapper();
    String stringifiedPostDTO = null;
    try {
      String json = mapper.writeValueAsString(dto);
      stringifiedPostDTO = json;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return stringifiedPostDTO;
  }
}
