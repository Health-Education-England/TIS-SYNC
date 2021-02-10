package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import uk.nhs.tis.sync.dto.PostDataDmsDto;

import static java.util.Optional.ofNullable;

public class PostDtoToDataDmsDtoMapper {

  public PostDataDmsDto postDtoToDataDmsDto(PostDTO postDto) {
    return new PostDataDmsDto(
        String.valueOf(postDto.getId()),
        postDto.getNationalPostNumber(),
        postDto.getStatus().toString().toUpperCase(),
        String.valueOf(postDto.getEmployingBodyId()),
        String.valueOf(postDto.getTrainingBodyId()),
        ofNullable(postDto.getOldPost()).map(PostDTO::getId).map(String::valueOf).orElse(null),
        ofNullable(postDto.getNewPost()).map(PostDTO::getId).map(String::valueOf).orElse(null),
        postDto.getOwner(),
        postDto.getIntrepidId()
    );
  }
}
