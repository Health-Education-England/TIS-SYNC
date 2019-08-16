package uk.nhs.tis.sync.event;

public class JobExecutionEvent {
  
  private final Object job;

  private final String message;
  
  public JobExecutionEvent(final Object job, final String message) {
    this.job = job;
    this.message = message;
  }
  public Object getJob() {
    return job;
  }
  public String getMessage() {
    return message;
  }

}
