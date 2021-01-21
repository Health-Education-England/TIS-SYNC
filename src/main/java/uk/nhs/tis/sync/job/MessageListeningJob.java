package uk.nhs.tis.sync.job;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordsRequest;
import com.amazonaws.services.kinesis.model.PutRecordsRequestEntry;
import com.amazonaws.services.kinesis.model.PutRecordsResult;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import com.transformuk.hee.tis.tcs.service.model.Post;
import com.transformuk.hee.tis.tcs.service.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.google.common.base.Stopwatch;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

@Component
@ManagedResource(objectName = "sync.mbean:name=PersonOwnerRebuildJob",
  description = "Job that listens for incoming messages")
public class MessageListeningJob {

  private static final Logger LOG = LoggerFactory.getLogger(PersonOwnerRebuildJob.class);

  private Stopwatch mainStopWatch;

  @Autowired
  private PostRepository repository;

  @Autowired
  private TcsServiceImpl tcsServiceImpl;

  @Autowired
  private AmazonKinesis amazonKinesis;

  @Scheduled(cron = "${application.cron.messageListeningJob}")
  @ManagedOperation(description = "Run MessageListening job")
  public void messageListeningJob() {
    runMessageListeningJob();
  }

  protected void runMessageListeningJob() {
    if (mainStopWatch != null) {
      LOG.info("Sync job [{}] already running, exiting this execution", getJobName());
      return;
    }
    CompletableFuture.runAsync(this::run);
  }

  protected void run() {
    try {
      LOG.info("Listening job [{}] started", getJobName());
      AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
      String queue_url = sqs.getQueueUrl("tis-trainee-sync-queue-preprod").getQueueUrl();
      List<Message> messages = sqs.receiveMessage(queue_url).getMessages();
      for(Message message : messages) {
        String messageBody = message.getBody();
        String[] keyValuePairs = messageBody.split(",");
        String tableName = keyValuePairs[0].split(":")[1];
        String postId = keyValuePairs[1].split(":")[1];
        LOG.info(tableName + " " + postId);

        PostDTO postDTO = getPostById(postId);

        sendDataIntoKinesisStream(postDTO);
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      mainStopWatch = null;
    }
  }

  private PostDTO getPostById(String id) {
    PostDTO postDTO = null;
    try {
      postDTO = tcsServiceImpl.getPostById(Long.parseLong(id));
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return postDTO;
  }

  private void sendDataIntoKinesisStream(PostDTO postDTO) {
    PutRecordsRequest putRecordsRequest  = new PutRecordsRequest();
    putRecordsRequest.setStreamName("tis-stage-mysql-cdc-stream");
    List <PutRecordsRequestEntry> putRecordsRequestEntryList  = new ArrayList<>();

    PutRecordsRequestEntry putRecordsRequestEntry  = new PutRecordsRequestEntry();
    putRecordsRequestEntry.setData(ByteBuffer.wrap(stringifyPostDto(postDTO).getBytes()));
    putRecordsRequestEntry.setPartitionKey(String.format("partitionKey-%d", putRecordsRequestEntryList.size()));
    putRecordsRequestEntryList.add(putRecordsRequestEntry);

    putRecordsRequest.setRecords(putRecordsRequestEntryList);
    PutRecordsResult putRecordsResult  = amazonKinesis.putRecords(putRecordsRequest);
    System.out.println("Put Result" + putRecordsResult);
  }


  private String stringifyPostDto(PostDTO postDTO) {
    ObjectMapper mapper = new ObjectMapper();
    String stringifiedPostDTO = null;
    try {
      String json = mapper.writeValueAsString(postDTO);
      stringifiedPostDTO = json;
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return stringifiedPostDTO;
  }

  private String getJobName() {
    return "MessageListeningJob";
  }

  @ManagedOperation(description = "Is the Message Listening job currently running")
  public boolean isCurrentlyRunning() {
    return mainStopWatch != null;
  }

  @ManagedOperation(description = "The current elapsed time of the current sync job")
  public String elapsedTime() {
    return mainStopWatch != null ? mainStopWatch.toString() : "0s";
  }

}
