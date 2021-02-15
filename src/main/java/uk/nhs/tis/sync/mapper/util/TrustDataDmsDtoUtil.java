package uk.nhs.tis.sync.mapper.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TrustDataDmsDtoUtil {

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  public @interface Status {

  }

  @Status
  public String status(com.transformuk.hee.tis.reference.api.enums.Status status) {
    return status.toString().toUpperCase();
  }
}