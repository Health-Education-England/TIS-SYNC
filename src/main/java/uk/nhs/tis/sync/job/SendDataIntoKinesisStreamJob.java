package uk.nhs.tis.sync.job;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class SendDataIntoKinesisStreamJob {

  private String KINESIS_STREAM_NAME = "azure-stage-db-to-aws-kinesis-continuous";

  private static final Logger LOG = LoggerFactory.getLogger(SendDataIntoKinesisStreamJob.class);

  private AmazonKinesis amazonKinesis;

  public SendDataIntoKinesisStreamJob(AmazonKinesis amazonKinesis) {
    this.amazonKinesis = amazonKinesis;
  }

  protected void sendDataIntoKinesisStream(Object dto) {
    PutRecordsRequest putRecordsRequest  = new PutRecordsRequest();
    putRecordsRequest.setStreamName(KINESIS_STREAM_NAME);
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
