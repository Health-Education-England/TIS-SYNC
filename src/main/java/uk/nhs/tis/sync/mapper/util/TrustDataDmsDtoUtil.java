package uk.nhs.tis.sync.mapper.util;

import org.springframework.stereotype.Component;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
public class TrustDataDmsDtoUtil {

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  public @interface Id {

  }

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  public @interface Status {

  }

  @Id
  public String id(Long id) {
    return String.valueOf(id);
  }

  @Status
  public String status(com.transformuk.hee.tis.reference.api.enums.Status status) {
    return status.toString().toUpperCase();
  }
}
