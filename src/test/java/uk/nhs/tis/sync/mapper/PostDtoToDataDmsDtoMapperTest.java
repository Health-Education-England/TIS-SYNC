package uk.nhs.tis.sync.mapper;
import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import com.transformuk.hee.tis.tcs.api.enumeration.Status;
import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.util.ReflectionUtils;
import uk.nhs.tis.sync.dto.PostDataDmsDto;
import uk.nhs.tis.sync.mapper.util.PostDataDmsDtoUtil;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostDtoToDataDmsDtoMapperTest {

  private PostDtoToDataDmsDtoMapper mapper;

  private PostDTO postDto;

  @Before
  public void setUp() {
    mapper = new PostDtoToDataDmsDtoMapperImpl();
    Field field = ReflectionUtils.findField(PostDtoToDataDmsDtoMapperImpl.class,
        "postDataDmsDtoUtil");
    field.setAccessible(true);
    ReflectionUtils.setField(field, mapper, new PostDataDmsDtoUtil());

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
    PostDataDmsDto postDataDmsDto = mapper.postDtoToPostDataDmsDto(postDto);

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
