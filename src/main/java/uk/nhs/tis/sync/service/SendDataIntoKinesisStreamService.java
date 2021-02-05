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

import com.transformuk.hee.tis.reference.api.dto.TrustDTO;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.stereotype.Service;
import uk.nhs.tis.sync.dto.OutputDto;
import uk.nhs.tis.sync.dto.MetadataDto;

@Service
@EnableWebSecurity(debug = false)
public class SendDataIntoKinesisStreamService {

  private static final Logger LOG = LoggerFactory.getLogger(SendDataIntoKinesisStreamService.class);

  private static final String SCHEMA_TCS = "tcs";

  private static final String SCHEMA_REFERENCE = "reference";

  private AmazonKinesis amazonKinesis;

  private String kinesisStreamName;

  private ObjectMapper objectMapper;

  /**
   * An object to send data into a Kinesis data stream.
   * @param amazonKinesis Object needed to a PutRecordsRequest object into the stream.
   * @param kinesisStreamName Name of the Kinesis stream.
   */
  public SendDataIntoKinesisStreamService(
      AmazonKinesis amazonKinesis,
      @Value("${application.aws.kinesis.streamName}") String kinesisStreamName) {
    this.amazonKinesis = amazonKinesis;
    this.kinesisStreamName = kinesisStreamName;
    this.objectMapper = new ObjectMapper();
  }

  /**
   * To send data into a stream it's necessary to have data as a string (in json format in this
   * case), wrapped as bytes in a ByteBuffer, and set as data in a PutRecordRequestEntry object,
   * which is part of a list of entries, which can be set as records in a PutRecordRequest
   * object, which can be sent into a stream by the amazonKinesis object.
   * @param dto   The retrieved dto that is being sent into the stream.
   * @param table The name of the table that dto belongs to.
   */
  public void sendData(Object dto, String table) {
    PutRecordsRequest putRecordsRequest  = new PutRecordsRequest();

    putRecordsRequest.setStreamName(kinesisStreamName);

    List<PutRecordsRequestEntry> putRecordsRequestEntryList  = new ArrayList<>();

    PutRecordsRequestEntry putRecordsRequestEntry  = new PutRecordsRequestEntry();

    String jsonStringOutput = buildDataOutput(dto, table);
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

  /**
   * A method to build the string in json format that will represent the data output that will be
   * sent into the stream.
   * @param dto   The retrieved dto that will be sent as a string into a kinesis stream.
   * @param table The table that object belonged to.
   * @return      The string in json format that will be sent into a kinesis stream.
   */
  private String buildDataOutput(Object dto, String table) {
    String schema = "";

    if (table.equals("Post")) {
      schema = SCHEMA_TCS;
    } else if (table.equals("Trust")) {
      schema = SCHEMA_REFERENCE;
    }

    MetadataDto metadataDto = new MetadataDto(schema, table, "load");
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
