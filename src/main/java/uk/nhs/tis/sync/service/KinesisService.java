package uk.nhs.tis.sync.service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kinesis.KinesisClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;
import software.amazon.awssdk.services.kinesis.model.PutRecordsResponse;
import uk.nhs.tis.sync.dto.DmsDto;

/**
 * A service which sends DMS DTOs to a Kinesis stream for processing.
 */
@Slf4j
@Service
public class KinesisService {

  private final KinesisClient amazonKinesis;
  private final ObjectMapper objectMapper;

  /**
   * An object to send data into a Kinesis data stream.
   *
   * @param amazonKinesis Object needed to a PutRecordsRequest object into the stream.
   */
  public KinesisService(
      KinesisClient amazonKinesis) {
    this.amazonKinesis = amazonKinesis;
    this.objectMapper = JsonMapper.builder()
        // Values are read as strings from kinesis, convert all numbers to string values.
        .configure(JsonWriteFeature.WRITE_NUMBERS_AS_STRINGS, true)
        .serializationInclusion(Include.NON_NULL)
        .addModule(new JavaTimeModule())
        // The date format is required but appears to be ignored.
        .defaultDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"))
        .build();
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
    List<PutRecordsRequestEntry> putRecordsRequestEntryList = new ArrayList<>();

    try {
      for (DmsDto dmsDto : dmsDtoList) {
        String jsonString = objectMapper.writeValueAsString(dmsDto);
        log.info("Trying to send{}", jsonString);

        String partitionKey = String.format("%s.%s",
            dmsDto.getMetadata().getSchemaName(), dmsDto.getMetadata().getTableName());
        PutRecordsRequestEntry putRecordsRequestEntry = PutRecordsRequestEntry.builder().data(
                SdkBytes.fromByteArray(jsonString.getBytes())).partitionKey(partitionKey)
            .build();
        putRecordsRequestEntryList.add(putRecordsRequestEntry);
      }
      PutRecordsRequest putRecordsRequest = PutRecordsRequest.builder()
          .streamName(kinesisStreamName)
          .records(putRecordsRequestEntryList).build();
      PutRecordsResponse putRecordsResult = amazonKinesis.putRecords(putRecordsRequest);

      log.info("Put Result {}", putRecordsResult);
    } catch (JsonProcessingException e) {
      log.info(e.getMessage());
    }
  }
}
