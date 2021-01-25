package uk.nhs.tis.sync.job;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transformuk.hee.tis.tcs.client.service.impl.TcsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.google.common.base.Stopwatch;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import uk.nhs.tis.sync.dto.AmazonSQSMessageDto;
import uk.nhs.tis.sync.service.ReconciliationService;

@Component
@ManagedResource(objectName = "sync.mbean:name=SyncHandlingJob",
  description = "Job that handles data input into a kinesis stream based on info received via Amazon SQS message")
public class SyncHandlingJob {

  private String QUEUE_NAME = "tis-trainee-sync-queue-preprod";

  private static final Logger LOG = LoggerFactory.getLogger(SyncHandlingJob.class);

  private Stopwatch mainStopWatch;

  @Autowired
  private TcsServiceImpl tcsServiceImpl;

  @Autowired
  AmazonKinesis amazonKinesis;

  private SendDataIntoKinesisStreamJob sendDataIntoKinesisStreamJob;

  @Autowired
  private ObjectMapper objectMapper;

  private AmazonSQS sqs;

  private ReconciliationService reconciliationService;

  private String queue_url;

  @Scheduled(cron = "${application.cron.syncHandlingJob}")
  @ManagedOperation(description = "Run Sync Handling Job")
  public void syncHandlingJob() {
    runMessageListeningJob();
    this.sqs = AmazonSQSClientBuilder.defaultClient();
    this.queue_url = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
    this.reconciliationService = new ReconciliationService(tcsServiceImpl);
    this.sendDataIntoKinesisStreamJob = new SendDataIntoKinesisStreamJob(amazonKinesis);
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
      LOG.info("Reading [{}] started", getJobName());
      List<Message> messages = sqs.receiveMessage(queue_url).getMessages();
      for(Message message : messages) {
        String messageBody = message.getBody();
        AmazonSQSMessageDto amazonSQSMessageDto = objectMapper.readValue(messageBody, AmazonSQSMessageDto.class);

        LOG.info(messageBody);

        Object dto = reconciliationService.retrieveDTO(amazonSQSMessageDto);

        sendDataIntoKinesisStreamJob.sendDataIntoKinesisStream(dto);
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
