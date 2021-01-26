package uk.nhs.tis.sync.job;

import java.util.List;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Stopwatch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.dto.AmazonSqsMessageDto;
import uk.nhs.tis.sync.service.DataRequestService;
import uk.nhs.tis.sync.service.SendDataIntoKinesisService;
import java.util.concurrent.CompletableFuture;

@Component
@ManagedResource(objectName = "sync.mbean:name=SyncHandlingJob",
    description = "Job that parses an sqs message, sends data accordingly into a Kinesis stream")
public class SyncHandlingJob {

  private static final Logger LOG = LoggerFactory.getLogger(SyncHandlingJob.class);

  private String JOB_NAME = "Sync Handling job";

  private Stopwatch mainStopWatch;

  private SendDataIntoKinesisService sendDataIntoKinesisService;

  private ObjectMapper objectMapper;

  private AmazonSQS sqs;

  private DataRequestService dataRequestService;

  private String queueName;

  public SyncHandlingJob(SendDataIntoKinesisService sendDataIntoKinesisService,
                         DataRequestService dataRequestService,
                         ObjectMapper objectMapper,
                         AmazonSQS sqs,
                         @Value("${application.aws.sqs.queueName}") String queueName) {
    this.sendDataIntoKinesisService = sendDataIntoKinesisService;
    this.dataRequestService = dataRequestService;
    this.objectMapper = objectMapper;
    this.sqs = sqs;
    this.queueName = queueName;
  }

  @Scheduled(cron = "${application.cron.syncHandlingJob}")
  @ManagedOperation(description = "Run Sync Handling Job")
  public void syncHandlingJob() {
    runSyncHandlingJob();
  }

  protected void runSyncHandlingJob() {
    if (mainStopWatch != null) {
      LOG.info("Sync job [{}] already running, exiting this execution", JOB_NAME);
      return;
    }
    CompletableFuture.runAsync(this::run);
  }

  protected void run() {
    try {
      LOG.info("Reading [{}] started", JOB_NAME);
      mainStopWatch = Stopwatch.createStarted();
      String queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
      List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
      for (Message message : messages) {
        String messageBody = message.getBody();
        AmazonSqsMessageDto messageDto = objectMapper
          .readValue(messageBody, AmazonSqsMessageDto.class);

        LOG.info(messageBody);

        Object dto = dataRequestService.retrieveDto(messageDto);

        sendDataIntoKinesisService.sendDataIntoKinesisStream(dto);
        mainStopWatch.stop();
        mainStopWatch = null;
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      mainStopWatch = null;
    }
  }

  @ManagedOperation(description = "Is the Sync Handling job currently running")
  public boolean isCurrentlyRunning() {
    return mainStopWatch != null;
  }

}
