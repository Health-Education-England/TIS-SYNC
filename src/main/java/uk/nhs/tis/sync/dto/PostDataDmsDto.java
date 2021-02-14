package uk.nhs.tis.sync.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.beans.ConstructorProperties;
import lombok.Data;

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
   */
  @ConstructorProperties({ "id", "nationalPostNumber", "status", "employingBodyId",
      "trainingBodyId", "oldPostId", "newPostId", "owner", "intrepidId" })
  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public PostDataDmsDto() {
    //Constructor for a PostDataDmsDto
  }
}
