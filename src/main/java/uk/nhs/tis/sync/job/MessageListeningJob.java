package uk.nhs.tis.sync.job;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  @Scheduled(cron = "${application.cron.messageListeningJob}")
//  @SchedulerLock(name = "messageListeningScheduledTask", lockAtLeastFor = FIFTEEN_MIN,
//    lockAtMostFor = FIFTEEN_MIN)
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
        LOG.info(message.toString());
      }

//      if (applicationEventPublisher != null) {
//        applicationEventPublisher
//          .publishEvent(new JobExecutionEvent(this, "Sync [" + getJobName() + "] started."));
//      }
//      mainStopWatch = Stopwatch.createStarted();
//
//      personRepository.buildPersonView();
//
//      LOG.info("Sync job [{}] finished. Total time taken {} to rebuild the table", getJobName(),
//        mainStopWatch.stop().toString());
//      mainStopWatch = null;
//      if (applicationEventPublisher != null) {
//        applicationEventPublisher
//          .publishEvent(new JobExecutionEvent(this, "Sync [" + getJobName() + "] finished."));
//      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      mainStopWatch = null;
//      if (applicationEventPublisher != null) {
//        applicationEventPublisher.publishEvent(new JobExecutionEvent(this, "<!channel> Sync ["
//          + getJobName() + "] failed with exception [" + e.getMessage() + "]."));
//      }
//      throw e;
    }
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
