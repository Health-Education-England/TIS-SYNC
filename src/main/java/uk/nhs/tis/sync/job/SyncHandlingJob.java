package uk.nhs.tis.sync.job;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.google.common.base.Stopwatch;
import uk.nhs.tis.sync.dto.AmazonSQSMessageDto;
import uk.nhs.tis.sync.service.DataRequestService;
import uk.nhs.tis.sync.service.SendDataIntoKinesisService;

@Component
@ManagedResource(objectName = "sync.mbean:name=SyncHandlingJob",
  description = "Job that handles data input into a kinesis stream based on info received via Amazon SQS message")
public class SyncHandlingJob {

  private static final Logger LOG = LoggerFactory.getLogger(SyncHandlingJob.class);

  private Stopwatch mainStopWatch;

  private SendDataIntoKinesisService sendDataIntoKinesisService;

  private ObjectMapper objectMapper;

  private AmazonSQS sqs;

  private DataRequestService dataRequestService;

  private String queueUrl;

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
      LOG.info("Sync job [{}] already running, exiting this execution", getJobName());
      return;
    }
    CompletableFuture.runAsync(this::run);
  }

  protected void run() {
    try {
      LOG.info("Reading [{}] started", getJobName());
      GetQueueUrlResult getQueueUrlResult = sqs.getQueueUrl(queueName);
      queueUrl = getQueueUrlResult.getQueueUrl();
      List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
      for(Message message : messages) {
        String messageBody = message.getBody();
        AmazonSQSMessageDto amazonSQSMessageDto = objectMapper.readValue(messageBody, AmazonSQSMessageDto.class);

        LOG.info(messageBody);

        Object dto = dataRequestService.retrieveDTO(amazonSQSMessageDto);

        sendDataIntoKinesisService.sendDataIntoKinesisStream(dto);
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      mainStopWatch = null;
    }
  }

  private String getJobName() {
    return "Sync Handling job";
  }

  @ManagedOperation(description = "Is the Sync Handling job currently running")
  public boolean isCurrentlyRunning() {
    return mainStopWatch != null;
  }

  @ManagedOperation(description = "The current elapsed time of the current sync job")
  public String elapsedTime() {
    return mainStopWatch != null ? mainStopWatch.toString() : "0s";
  }

}
