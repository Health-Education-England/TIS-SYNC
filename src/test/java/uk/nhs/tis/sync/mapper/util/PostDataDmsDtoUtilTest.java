package uk.nhs.tis.sync.mapper.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.Before;
import org.junit.Test;

public class PostDataDmsDtoUtilTest {

  private PostDataDmsDtoUtil util;

  @Before
  public void setup() {
    util = new PostDataDmsDtoUtil();
  }

  @Test
  public void shouldReturnStringValueOfStatus() {
    Status currentStatus = Status.CURRENT;
    assertEquals("CURRENT", util.status(currentStatus));
  }

  @Test
  public void givenAPostDtoShouldExtractItsEmployingBodyIdOrTrainingBodyId() {
    PostDTO postDto = new PostDTO();
    postDto.setId(10L);

    assertEquals("10", util.postId(postDto));
  }
}
