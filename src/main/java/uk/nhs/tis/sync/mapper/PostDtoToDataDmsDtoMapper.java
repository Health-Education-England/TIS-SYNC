package uk.nhs.tis.sync.mapper;

import com.transformuk.hee.tis.tcs.api.dto.PostDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import uk.nhs.tis.sync.dto.PostDataDmsDto;
import uk.nhs.tis.sync.mapper.util.PostDataDmsDtoUtil;
import uk.nhs.tis.sync.mapper.util.PostDataDmsDtoUtil.Id;
import uk.nhs.tis.sync.mapper.util.PostDataDmsDtoUtil.PostId;
import uk.nhs.tis.sync.mapper.util.PostDataDmsDtoUtil.Status;

@Mapper(componentModel = "spring", uses = PostDataDmsDtoUtil.class)
public interface PostDtoToDataDmsDtoMapper {

  @Mapping(target="id", source = "postDto.id", qualifiedBy = Id.class)
  @Mapping(target="nationalPostNumber", source = "postDto.nationalPostNumber")
  @Mapping(target="status", source = "postDto.status", qualifiedBy = Status.class)
  @Mapping(target="employingBodyId", source = "postDto.employingBodyId", qualifiedBy =
      Id.class)
  @Mapping(target="trainingBodyId", source = "postDto.trainingBodyId", qualifiedBy =
      Id.class)
  @Mapping(target="oldPostId", source = "postDto.oldPost", qualifiedBy = PostId.class)
  @Mapping(target="newPostId", source = "postDto.newPost", qualifiedBy = PostId.class)
  @Mapping(target="owner", source = "postDto.owner")
  @Mapping(target="intrepidId", source = "postDto.intrepidId")
  public PostDataDmsDto postDtoToPostDataDmsDto(PostDTO postDto);
}
