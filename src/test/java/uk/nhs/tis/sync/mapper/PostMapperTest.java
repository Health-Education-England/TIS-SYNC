package uk.nhs.tis.sync.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.Before;
import org.junit.Test;
import uk.nhs.tis.sync.dto.PostDmsDto;

public class PostMapperTest {

  private PostMapper mapper;

  private PostDTO postDto;

  @Before
  public void setUp() {
    mapper = new PostMapperImpl();

    PostDTO newPost = new PostDTO();
    newPost.setId(184668L);

    PostDTO oldPost = new PostDTO();
    oldPost.setId(5L);

    postDto = new PostDTO();
    postDto.setId(44381L);
    postDto.setNationalPostNumber("EAN/8EJ83/094/SPR/001");
    postDto.setStatus(Status.CURRENT);
    postDto.setEmployingBodyId(287L);
    postDto.setTrainingBodyId(1464L);
    postDto.setNewPost(newPost);
    postDto.setOldPost(oldPost);
    postDto.setOwner("Health Education England North West London");
    postDto.setIntrepidId("128374444");
  }

  @Test
  public void shouldMapAPostDtoToADataDmsDto() {
    PostDmsDto postDmsDto = mapper.toDmsDto(postDto);

    assertEquals("44381", postDmsDto.getId());
    assertEquals("EAN/8EJ83/094/SPR/001", postDmsDto.getNationalPostNumber());
    assertEquals("CURRENT", postDmsDto.getStatus());
    assertEquals("287", postDmsDto.getEmployingBodyId());
    assertEquals("1464", postDmsDto.getTrainingBodyId());
    assertEquals("184668", postDmsDto.getNewPostId());
    assertEquals("5", postDmsDto.getOldPostId());
    assertEquals("Health Education England North West London", postDmsDto.getOwner());
    assertEquals("128374444", postDmsDto.getIntrepidId());
  }
}
