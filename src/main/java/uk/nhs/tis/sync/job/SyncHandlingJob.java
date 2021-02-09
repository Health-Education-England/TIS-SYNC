package uk.nhs.tis.sync.job;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.nhs.tis.sync.dto.AmazonSqsMessageDto;
import uk.nhs.tis.sync.service.DataRequestService;
import uk.nhs.tis.sync.service.KinesisService;

@Component
@ManagedResource(objectName = "sync.mbean:name=SyncHandlingJob",
    description = "Job that parses an sqs message, sends data accordingly into a Kinesis stream")
public class SyncHandlingJob {

  private static final Logger LOG = LoggerFactory.getLogger(SyncHandlingJob.class);

  private static final String JOB_NAME = "Sync Handling job";

  private KinesisService kinesisService;

  private ObjectMapper objectMapper;

  private AmazonSQS sqs;

  private DataRequestService dataRequestService;

  private String queueUrl;

  /**
   * A job that reads queue messages, interprets what dto is being requested, fetches it, and
   * sends it as data into a kinesis stream.
   * @param kinesisService A service responsible for outputting into the stream.
   * @param dataRequestService         A service wrapping TcsServiceImpl to fetch a dto.
   * @param objectMapper               To map a message into an AmazonSqsMessageDto.
   * @param sqs                        An AmazonSQS object to interact with a queue.
   * @param queueUrl                   The url of the queue to interact with.
   */
  public SyncHandlingJob(KinesisService kinesisService,
                         DataRequestService dataRequestService, ObjectMapper objectMapper,
                         AmazonSQS sqs,
                         @Value("${application.aws.sqs.queueUrl}") String queueUrl) {
    this.kinesisService = kinesisService;
    this.dataRequestService = dataRequestService;
    this.objectMapper = objectMapper;
    this.sqs = sqs;
    this.queueUrl = queueUrl;
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
      List<Message> messages = sqs.receiveMessage(queueUrl).getMessages();
      for (Message message : messages) {
        String messageBody = message.getBody();
        AmazonSqsMessageDto messageDto = objectMapper
            .readValue(messageBody, AmazonSqsMessageDto.class);

        LOG.info(messageBody);

        Object dto = dataRequestService.retrieveDto(messageDto);

        if (dto != null) {
          kinesisService.sendData(dto);
        }
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
}
