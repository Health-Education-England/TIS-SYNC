package uk.nhs.tis.sync.mapper;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.PostSuffix;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import com.transformuk.hee.tis.tcs.service.model.Post;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import uk.nhs.tis.sync.dto.PostDataDmsDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostDtoToDataDmsDtoMapperTest {

  private PostDtoToDataDmsDtoMapper mapper;

  private PostDTO postDto;

  @Before
  public void setUp() {
    mapper = new PostDtoToDataDmsDtoMapper();

    PostDTO newPost = new PostDTO();
    newPost.setId(184668L);

    PostDTO oldPost = new PostDTO();
    oldPost.setId(5L);

    postDto = new PostDTO();
    postDto.setId(44381L);
    postDto.setNationalPostNumber("EAN/8EJ83/094/SPR/001");
    postDto.status(Status.CURRENT);
    postDto.employingBodyId(287L);
    postDto.trainingBodyId(1464L);
    postDto.newPost(newPost);
    postDto.oldPost(oldPost);
    postDto.owner("Health Education England North West London");
    postDto.intrepidId("128374444");
  }

  @Test
  public void shouldMapAPostDtoToADataDmsDto() {
    PostDataDmsDto postDataDmsDto = mapper.postDtoToDataDmsDto(postDto);

    assertEquals("44381", postDataDmsDto.getId());
    assertEquals("EAN/8EJ83/094/SPR/001", postDataDmsDto.getNationalPostNumber());
    assertEquals("CURRENT", postDataDmsDto.getStatus());
    assertEquals("287", postDataDmsDto.getEmployingBodyId());
    assertEquals("1464", postDataDmsDto.getTrainingBodyId());
    assertEquals("184668", postDataDmsDto.getNewPostId());
    assertEquals("5", postDataDmsDto.getOldPostId());
    assertEquals("Health Education England North West London", postDataDmsDto.getOwner());
    assertEquals("128374444", postDataDmsDto.getIntrepidId());
  }

}
