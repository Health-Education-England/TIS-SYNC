package uk.nhs.tis.sync.job;


import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private SendDataIntoKinesisService sendDataIntoKinesisService;

  private ObjectMapper objectMapper;

  private AmazonSQS sqs;

  private DataRequestService dataRequestService;

  private String queueName;

  /**
   *
   * @param sendDataIntoKinesisService A service responsible for outputting into the stream.
   * @param dataRequestService A service wrapping TcsServiceImpl to fetch a dto.
   * @param objectMapper To map a message into an AmazonSqsMessageDto.
   * @param sqs An AmazonSQS object to interact with a queue.
   * @param queueName The name of the queue to interact with.
   */
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
    CompletableFuture.runAsync(this::run);
  }

  protected void run() {
    try {
      LOG.info("Reading [{}] started", JOB_NAME);
      String queueUrl = sqs.getQueueUrl(queueName).getQueueUrl();
      List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
      for (Message message : messages) {
        String messageBody = message.getBody();
        AmazonSqsMessageDto messageDto = objectMapper
            .readValue(messageBody, AmazonSqsMessageDto.class);

        LOG.info(messageBody);

        Object dto = dataRequestService.retrieveDto(messageDto);

        sendDataIntoKinesisService.sendDataIntoKinesisStream(dto, messageDto.getTable());
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
}
