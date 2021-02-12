package uk.nhs.tis.sync.mapper.util;

import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import org.springframework.stereotype.Component;

import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Component
public class PostDataDmsDtoUtil {

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

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.SOURCE)
  public @interface PostId {

  }

  @Id
  public String id(Long id) {
    return String.valueOf(id);
  }

  @Status
  public String status(Status status) {
    return status.toString().toUpperCase();
  }

  @PostId
  public String postId(PostDTO postDto) {
    return postDto != null ? String.valueOf(postDto.getId()) : null;
  }
}
