package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import uk.nhs.tis.sync.dto.PostDmsDto;

@Mapper(componentModel = "spring")
public interface PostMapper {

  @Mapping(target = "oldPostId", source = "postDto.oldPost.id")
  @Mapping(target = "newPostId", source = "postDto.newPost.id")
  PostDmsDto toDmsDto(PostDTO postDto);
}
