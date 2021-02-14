package uk.nhs.tis.sync.mapper.util;

import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Qualifier;

import org.springframework.stereotype.Component;

@Component
public class PostDataDmsDtoUtil {

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  public @interface Status {

  }

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  public @interface PostId {

  }

  @Status
  public String status(com.transformuk.hee.tis.tcs.api.enumeration.Status status) {
    return status.toString().toUpperCase();
  }

  @PostId
  public String postId(PostDTO postDto) {
    return postDto != null ? String.valueOf(postDto.getId()) : null;
  }
}
