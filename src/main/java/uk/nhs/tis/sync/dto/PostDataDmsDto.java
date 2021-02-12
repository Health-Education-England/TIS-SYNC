package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Value;

import java.beans.ConstructorProperties;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PostDataDmsDto {

  String id;
  String nationalPostNumber;
  String status;
  String employingBodyId;
  String trainingBodyId;
  String oldPostId;
  String newPostId;
  String owner;
  String intrepidId;

  /**
   * Constructor for a PostDataDmsDto, an object which represents the "data" portion of a record.
   * A DmsDto will have it as a field along with a MetadataDto. It's meant to reflect only the
   * relevant fields of a PostDto.
   * @param id                 Same id as in the PostDto, but in String form
   * @param nationalPostNumber Same nationalPostNumber as in the PostDto
   * @param status             The string value of the status in the PostDto (e.g. "CURRENT")
   * @param employingBodyId    Same employingBodyId as in the PostDto, but in String form
   * @param trainingBodyId     Same trainingBodyId as in the PostDto, but in String form
   * @param oldPostId          The id of the oldPost of a PostDto, and in String form
   * @param newPostId          The id of the newPost of a PostDto, and in String form
   * @param owner              The owner as in the PostDto, but in String form
   * @param intrepidId         The intrepidId as in the PostDto, but in String form
   */
  @ConstructorProperties({ "id", "nationalPostNumber", "status", "employingBodyId",
      "trainingBodyId", "oldPostId", "newPostId", "owner", "intrepidId" })
  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public PostDataDmsDto() {
  }
}
